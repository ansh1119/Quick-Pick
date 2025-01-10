package com.example.quickpick.service

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.quickpick.models.College
import com.example.quickpick.models.MenuItem
import com.example.quickpick.models.Order
import com.example.quickpick.models.Outlet
import com.example.quickpick.models.User
import com.example.quickpick.setOutletId
import com.example.quickpick.utils.ResultState
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthService {


    private lateinit var mVerificationCode:String



    override fun createUserWithPhone(phone: String, activity:Activity): Flow<ResultState<String>> = callbackFlow {
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
        val credential = PhoneAuthProvider.getCredential(mVerificationCode,otp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    val currentUser = firebaseAuth.currentUser
                    Log.d("AUTH REPO",currentUser.toString())
                    if(currentUser!=null){
                        val phone=currentUser.phoneNumber
                        val documentRef = firestore.collection("User").document(
                            "+91${phone.toString()}"
                        )
                        documentRef.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    trySend(ResultState.Success("Signing In...."))
                                }
                                else{
                                    trySend(ResultState.Success("OTP Verified"))
                                }
                            }
                    }
                    else{
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
        collegeName: String
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
            menu = menuIds // Store list of MenuItem IDs
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

    override suspend fun getColleges(): List<College> {
        return try {
            val collegeSnapshot = firestore.collection("colleges").get().await()
            collegeSnapshot.toObjects(College::class.java)
        } catch (e: Exception) {
            // Handle error (e.g., log it, or return an empty list)
            emptyList()
        }
    }


    override suspend fun addMenuItem(outletId: String, name: String, price: Double, image: Uri?) {
        val firestore = FirebaseFirestore.getInstance()
        val menuItemsRef = firestore.collection("menu_items")
        val outletsRef = firestore.collection("outlets")

        try {
            // Step 1: Add the menu item to the "menu_items" collection
            val newMenuItemRef = menuItemsRef.document() // Auto-generate document ID

            // Create a MenuItem object with the provided details
            val menuItem = MenuItem(
                outletId = outletId,
                id = newMenuItemRef.id, // Set the auto-generated ID
                name = name,
                price = price,
                available = true, // Default to available
                image = image.toString()
            )

            // Convert the MenuItem object to a Map to store in Firestore
            val menuItemMap = hashMapOf(
                "outletId" to menuItem.outletId,
                "id" to menuItem.id,
                "name" to menuItem.name,
                "price" to menuItem.price,
                "available" to menuItem.available,
                "image" to menuItem.image?.toString() // Store the image URI as a string (optional)
            )

            newMenuItemRef.set(menuItemMap).await()

            // Step 2: Update the outlet's menu list with the new menu item ID
            val outletDocRef = outletsRef.document(outletId)
            val outletDocSnapshot = outletDocRef.get().await()

            if (outletDocSnapshot.exists()) {
                val currentMenu = outletDocSnapshot.get("menu") as? List<String> ?: emptyList()
                val updatedMenu = currentMenu + newMenuItemRef.id // Add the new menu item ID
                outletDocRef.update("menu", updatedMenu).await()
            } else {
                throw Exception("Outlet with ID $outletId not found")
            }
        } catch (e: Exception) {
            // Handle errors appropriately
            throw Exception("Error adding menu item: ${e.message}")
        }
    }






    override fun getOrdersForOutlet(outletId: String): Flow<ResultState<List<Order>>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereEqualTo("outletId", outletId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(ResultState.Failure(error))
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val orders = snapshots.documents.mapNotNull { it.toObject(Order::class.java) }
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





}