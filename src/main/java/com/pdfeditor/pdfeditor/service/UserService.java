package com.pdfeditor.pdfeditor.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.pdfeditor.pdfeditor.model.UserModel;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    public String createUser(UserModel user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("pdf_user").document(user.getId()).set(user);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public UserModel getUser(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("pdf_user" ).document(id);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        UserModel user;
        if(document.exists()) {
            user = document.toObject(UserModel.class);
            return user;
        }
        return null;
    }

    public String updateUser(UserModel userModel) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection( "pdf_user").document(userModel.getId()).set(userModel);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteUser(String id) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection( "pdf_user").document(id).delete();
        return "Successfully deleted " + id;
    }

}
