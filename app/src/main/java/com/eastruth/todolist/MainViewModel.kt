package com.eastruth.todolist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class MainViewModel : ViewModel() {

    val db = FirebaseFirestore.getInstance()

    // LiveData : observable data holder class = 관찰할 수 있는 데이터를 담아놓기 위한 클래스
    // 여기다 데이터를 담아두면 관찰하고 있다가 변경이 되면 화면을 다시 그려주는 역할을 하게 할 수 있다
    // 가장 큰 장점은 코드가 간결해진다
    // 화면을 갱신해야 될 시점을 다 찾아서 따로 관리할 필요가 없고
    // 코드들이 분산되어있는걸 한쪽으로 몰아넣을 수 있다
    val todoLiveData = MutableLiveData<List<DocumentSnapshot>>()
//    private val data = arrayListOf<DocumentSnapshot>()

    init {
        fetchData()
    }

    fun fetchData() {
        val user = FirebaseAuth.getInstance().currentUser ?: return //엘비스프레슬리

        // 실시간 업데이트
        db.collection(user.uid)
            .orderBy("text")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    todoLiveData.value = value.documents
                }

//                data.clear()
//                for (doc in value!!) { //!!:null이 절대 아니라고 가정
//                    data.add(doc)
//                }
//                todoLiveData.value = data
            }

//        db.collection(user.uid)
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    val todo = Todo(
//                        document.data["text"] as String,
//                        document.data["done"] as Boolean
//                    )
//                    data.add(todo)
//                }
//                todoLiveData.value = data
//            }
//            .addOnFailureListener { exception ->
//
//            }
    }

    fun toggleTodo(todo: DocumentSnapshot) {
        FirebaseAuth.getInstance().currentUser?.let {
            val isDone = todo.getBoolean("done") ?: false
            db.collection(it.uid).document(todo.id).update("done", !isDone)
        }

//        todo.isDone = !todo.isDone
//        todoLiveData.value = data
    }

    fun addTodo(todo: Todo) {

        // ?. : 안전한 호출 : null이 아닐때만 실행
        FirebaseAuth.getInstance().currentUser?.let {
            db.collection(it.uid).add(todo)
        }


//        db.collection("cities").document("new-city-id").set(data)
//        data.add(todo)
//        todoLiveData.value = data //LiveData 값을 최신 데이터로 변경하겠다
    }

    fun deleteTodo(todo: DocumentSnapshot) {
        FirebaseAuth.getInstance().currentUser?.let {
            db.collection(it.uid).document(todo.id).delete()
        }
//        data.remove(todo)
//        todoLiveData.value = data
    }
}