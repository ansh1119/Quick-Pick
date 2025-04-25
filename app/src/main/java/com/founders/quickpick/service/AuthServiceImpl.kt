package com.founders.quickpick.service

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import com.founders.quickpick.model.College
import com.founders.quickpick.model.MenuItem
import com.founders.quickpick.model.Order
import com.founders.quickpick.model.User
import com.founders.quickpick.setOutletId
import com.founders.quickpick.utils.ResultState
import com.founders.quickpick2.model.Outlet
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import coil.size.Scale
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID


class AuthServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthService {


    private lateinit var mVerificationCode: String


    override fun createUserWithPhone(phone: String, activity: Activity): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)

            val onVerificationCallback = object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    TODO("Not yet implemented")
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    trySend(ResultState.Failure(p0))
                }

                override fun onCodeSent(
                    verificationCode: String,
                    p1: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verificationCode, p1)
                    trySend(ResultState.Success("OTP Sent Successfully"))
                    mVerificationCode = verificationCode
                }

            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91$phone") // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(activity) // Activity (for callback binding)
                .setCallbacks(onVerificationCallback) // OnVerificationStateChangedCallbacks
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
            awaitClose {
                close()
            }
        }

    override fun signInWithCredential(otp: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        val credential = PhoneAuthProvider.getCredential(mVerificationCode, otp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    val currentUser = firebaseAuth.currentUser
                    Log.d("AUTH REPO", currentUser.toString())
                    if (currentUser != null) {
                        val phone = currentUser.phoneNumber
                        val documentRef = firestore.collection("users").document(
                            "${phone.toString()}"
                        )
                        documentRef.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    trySend(ResultState.Success("Signing In...."))
                                } else {
                                    trySend(ResultState.Success("OTP Verified"))
                                }
                            }
                    } else {
                        trySend(ResultState.Success("OTP Verified"))
                    }

                }
            }.addOnFailureListener {
                trySend(ResultState.Failure(it))
            }
        awaitClose {
            close()
        }
    }


    override suspend fun submitDetails(
        context: Context,
        userPhone: String,
        outletName: String,
        menuItems: List<MenuItem>,
        collegeName: String,
        razorpayId:String
    ) {
        // Check if the user already has an outlet
        val userRef = firestore.collection("users").document(userPhone)
        val userSnapshot = userRef.get().await()
        val user = userSnapshot.toObject(User::class.java)

        if (user?.outlet != null) {
            throw IllegalStateException("User already has an outlet.")
        }

        // Create Outlet Document and get its ID
        val outletRef = firestore.collection("outlets").document() // Auto-generated ID for outlet
        val outletId = outletRef.id // Get the generated ID of the outlet
        setOutletId(context,outletId)
        // Store only menu item IDs (instead of entire MenuItem object)
        val menuIds = menuItems.map { it.id ?: "" } // Extract IDs from MenuItem list
        val outlet = Outlet(
            id = outletId,
            ownerPhone = userPhone,
            collegeName=collegeName,
            outletName = outletName,
            menu = menuIds, // Store list of MenuItem IDs
            razorpayId = razorpayId
        )
        outletRef.set(outlet) // Set the outlet document

        // Update the user's outlet field
        val updatedUser = User(phone = userPhone, outlet = outletId)
        userRef.set(updatedUser, SetOptions.merge())

        val collegeRef = firestore.collection("colleges")
        val query = collegeRef.whereEqualTo("name", collegeName).get().await()

        if (query.isEmpty) {
            // Create a new College document with an auto-generated ID
            val newCollegeRef = collegeRef.document() // Get a reference with an auto-generated ID
            val newCollege = College(
                id = newCollegeRef.id, // Set the auto-generated ID in the College object
                name = collegeName,
                outlets = listOf(outletId) // Store only the outlet ID
            )
            newCollegeRef.set(newCollege) // Add the College document to Firestore
        } else {
            // Update the existing College with a new outlet ID
            val collegeDoc = query.documents.first()
            val collegeId = collegeDoc.id // Get the existing document ID
            val college = collegeDoc.toObject(College::class.java)

            val updatedCollege = college?.copy(
                id = collegeId, // Ensure the ID field is set if it's missing
                outlets = college.outlets?.plus(outletId)
            )

            // Save the updated college document back to Firestore
            collegeRef.document(collegeId).set(updatedCollege ?: College(id = collegeId, outlets = listOf(outletId)))
        }  }


    fun listenForOutletChanges(outletId: String, onOutletUpdated: (Outlet?) -> Unit) {
        val outletRef = firestore.collection("outlets").document(outletId)

        outletRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error if any
                Log.w("OutletListener", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // Convert the snapshot to an Outlet object
                val updatedOutlet = snapshot.toObject(Outlet::class.java)
                onOutletUpdated(updatedOutlet)
            } else {
                Log.d("OutletListener", "No such document")
            }
        }
    }


    override suspend fun getColleges(): List<College> {
        return try {
            val collegeSnapshot = firestore.collection("colleges").get().await()
            val colleges = collegeSnapshot.toObjects(College::class.java)
            Log.d("FirestoreData", "Fetched colleges: ${colleges.size}")
            colleges
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error fetching colleges", e)
            emptyList()
        }
    }


    override suspend fun addMenuItem(
        context: Context,
        outletId: String,
        name: String,
        price: Double,
        image: Uri?,
        category: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val menuItemsRef = firestore.collection("menu_items")
        val outletsRef = firestore.collection("outlets")

        try {
            val newMenuItemRef = menuItemsRef.document()
            val imageUrl = image?.let {
                val url = uploadImageToFirebaseStorage(it, context = context)
                Log.d("AddMenuItem", "Image uploaded: $url")
                url
            }

            val menuItem = MenuItem(
                category = category,
                outletId = outletId,
                id = newMenuItemRef.id,
                name = name,
                price = price,
                available = true,
                image = imageUrl ?: ""
            )

            newMenuItemRef.set(menuItem).await()
            Log.d("AddMenuItem", "Menu item added to Firestore: ${menuItem.name}")

            val outletDocRef = outletsRef.document(outletId)
            val outletDocSnapshot = outletDocRef.get().await()

            if (outletDocSnapshot.exists()) {
                val currentMenu = outletDocSnapshot.get("menu") as? List<String> ?: emptyList()
                val updatedMenu = currentMenu + newMenuItemRef.id
                outletDocRef.update("menu", updatedMenu).await()
                Log.d("AddMenuItem", "Menu updated for outlet: $outletId")
            } else {
                throw Exception("Outlet with ID $outletId not found")
            }
        } catch (e: Exception) {
            Log.e("AddMenuItem", "Error: ${e.message}")
            throw e
        }
    }

    suspend fun compressImage(context: Context, uri: Uri): File? {
        return withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(1000, 1000) // Set the target size (smaller size, faster upload)
                .scale(Scale.FIT)
                .build()

            val result = loader.execute(request)
            if (result is coil.request.SuccessResult) {
                val bitmap = result.drawable.toBitmap()
                val compressedFile = File(context.cacheDir, "compressed_image.jpg")
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    80,
                    compressedFile.outputStream()
                ) // 80% compression
                compressedFile
            } else {
                null
            }
        }
    }

    suspend fun uploadImageToFirebaseStorage(uri: Uri, context: Context): String? {
        return try {
            // Step 1: Compress the image before uploading (optional but recommended)
            val compressedFile = compressImage(context, uri)
            val fileUri = compressedFile?.toUri() ?: uri

            // Step 2: Define the storage path
            val storageRef = FirebaseStorage.getInstance().reference
            val uniqueFileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("images/$uniqueFileName")


            // Step 3: Upload the image with chunked/optimistic upload
                imageRef.putFile(fileUri).await()  // Automatically handles resumable uploads

            // Step 4: Retrieve and return the download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            downloadUrl
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Image upload failed: ${e.message}")
            null // Return null if upload fails
        }
    }


    override fun getOrdersForOutlet(outletId: String): Flow<ResultState<List<Order>>> =
        callbackFlow {
            val listener = firestore.collection("orders")
                .whereEqualTo("outletId", outletId)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        trySend(ResultState.Failure(error))
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val orders =
                            snapshots.documents.mapNotNull {
                                it.toObject(Order::class.java)
                            }
                                .filter { order ->
                                    order.status != "Picked" // Only include orders whose status is not "Picked"
                                }
                        trySend(ResultState.Success(orders))
                    }
                }

            awaitClose { listener.remove() }
        }

    override suspend fun fetchOrderDetails(orderId: String): Order? {
        return try {
            val snapshot = firestore.collection("orders")
                .document(orderId)
                .get()
                .await()
            snapshot.toObject(Order::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun markOrderAsReady(
        orderId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("orders").document(orderId)
            .update("status", "Ready")
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }


    override suspend fun getMenuItems(outletId: String): List<MenuItem> {
        return try {
            // Step 1: Fetch the list of menu item IDs from the outlet document
            val outletSnapshot = firestore.collection("outlets")
                .document(outletId)
                .get()
                .await()

            val menuItemIds = outletSnapshot.get("menu") as? List<String> ?: emptyList()

            // Step 2: Fetch the details of all menu items by their IDs
            if (menuItemIds.isNotEmpty()) {
                val menuItemSnapshots = firestore.collection("menu_items")
                    .whereIn(FieldPath.documentId(), menuItemIds)
                    .get()
                    .await()

                menuItemSnapshots.documents.map { document ->
                    document.toObject(MenuItem::class.java)!!.copy(id = document.id)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching menu items: ${e.message}")
            emptyList()
        }
    }

    // Function to update the availability of a menu item
    override suspend fun updateMenuItemAvailability(menuItemId: String, isAvailable: Boolean) {
        try {
            // Reference to the menu item document in Firestore
            val menuItemRef = firestore.collection("menu_items").document(menuItemId)

            // Update the "available" field
            menuItemRef.update("available", isAvailable).await()
            Log.d("Firestore", "Updated availability of menu item: $menuItemId to $isAvailable")

            // Optional: Log a confirmation
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating menu item availability: ${e.message}")
            throw e
        }
    }


    override suspend fun getOrdersForOutletInDateRange(
        outletId: String,
        startDate: Date,
        endDate: Date
    ): List<Order> {
        if (outletId.isBlank()) {
            throw IllegalArgumentException("Outlet ID cannot be blank.")
        }

        // Convert Java Dates to Unix timestamps in milliseconds
        val startTimestamp = startDate.time // Date to milliseconds
        val endTimestamp = endDate.time // Date to milliseconds

        Log.d(
            "FirestoreQuery",
            "Start Timestamp (ms): $startTimestamp, End Timestamp (ms): $endTimestamp"
        )

        // Query Firestore
        val ordersQuery = firestore.collection("orders")
            .whereEqualTo("outletId", outletId) // Match the outletId
            .whereGreaterThanOrEqualTo("timestamp", startTimestamp) // Start of range
            .whereLessThanOrEqualTo("timestamp", endTimestamp) // End of range

        return try {
            // Fetch documents asynchronously
            val querySnapshot = withContext(Dispatchers.IO) {
                ordersQuery.get().await()
            }

            Log.d("FirestoreQuery", "Documents found: ${querySnapshot.documents.size}")

            // Map Firestore documents to the Order data model
            querySnapshot.documents.mapNotNull { document ->
                val order = document.toObject(Order::class.java)
                Log.d("FirestoreQuery", "Order: $order")
                order
            }
        } catch (e: Exception) {
            Log.e("FirestoreQueryError", "Error fetching documents", e)
            emptyList()
        }
    }


    override suspend fun getOutlet(documentId: String): User? {
        return try {
            // Fetch the document with the given document ID
            val documentSnapshot: DocumentSnapshot = firestore.collection("users")
                .document(documentId)
                .get()
                .await()

            // Check if the document exists
            if (documentSnapshot.exists()) {
                // Parse the document into an Outlet object
                documentSnapshot.toObject(User::class.java)
            } else {
                Log.e("Firestore", "Document does not exist")
                null
            }
        } catch (e: Exception) {
            // Handle any errors
            Log.e("Firestore", "Error fetching document: ${e.message}")
            null
        }
    }


//    fun listenForNewOrders(outletId: String) {
//        FirebaseFirestore.getInstance().collection("orders")
//            .whereEqualTo("outletId", outletId)
//            .addSnapshotListener { snapshots, error ->
//                if (error != null) {
//                    Log.e("OrderListener", "Error listening for orders", error)
//                    return@addSnapshotListener
//                }
//
//                snapshots?.documentChanges?.forEach { change ->
//                    if (change.type == DocumentChange.Type.ADDED) {
//                        val newOrder = change.document.toObject(Order::class.java)
//                        sendNotification(newOrder)
//                    }
//                }
//            }
//    }
//
//    fun sendNotification(context:Context,order: Order) {
//        val channelId = "order_notifications"
//        val notificationId = System.currentTimeMillis().toInt()
//
//        val intent = Intent(context, OrdersActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//        val pendingIntent = PendingIntent.getActivity(
//            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notificationBuilder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.logo)
//            .setContentTitle("New Order Received!")
//            .setContentText("Order #${order.id} has been placed.")
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Order Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        notificationManager.notify(notificationId, notificationBuilder.build())
//    }
//


}