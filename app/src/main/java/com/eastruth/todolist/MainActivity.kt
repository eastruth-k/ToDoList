package com.eastruth.todolist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eastruth.todolist.databinding.ActivityMainBinding
import com.eastruth.todolist.databinding.ItemTodoBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot

class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1;
    private lateinit var binding: ActivityMainBinding

   // ViewModel : 라이프사이클에 관계없이 데이터 저장 가능하게 하기 위해서 만들어졌다
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("ddong MainActivity onCreate")

        val pi = packageManager.getPackageInfo(packageName, 0)
        println("ddong versionCode : ${pi.versionCode}")
        println("ddong versionName : ${pi.versionName}")
        Toast.makeText(applicationContext, "${pi.versionCode} : ${pi.versionName}", Toast.LENGTH_LONG).show()

        // setContentView(R.layout.activity_main) 대신에 view binding 사용
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 로그인 안됨
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (FirebaseAuth.getInstance().currentUser == null) {
            login()
        } else {
            println("ddong MainActivity user name : " + user?.displayName)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TodoAdapter(
                emptyList(),
                {
                    viewModel.deleteTodo(it)
//                    binding.recyclerView.adapter?.notifyDataSetChanged()
                },
                {
                    viewModel.toggleTodo(it)
//                    binding.recyclerView.adapter?.notifyDataSetChanged()
                })
        }

        binding.btAdd.setOnClickListener {
            val todo = Todo(binding.etTodo.text.toString())
            viewModel.addTodo(todo);
            binding.etTodo.setText("")
//            binding.recyclerView.adapter?.notifyDataSetChanged()
        }


        //관찰하고 UI업데이트 함
        viewModel.todoLiveData.observe(this, Observer {
            (binding.recyclerView.adapter as TodoAdapter).setData(it)
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
//                val user = FirebaseAuth.getInstance().currentUser
//                viewModel.fetchData()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    fun login() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN)
    }

    fun logout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                login()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_log_out -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

// val : 읽기 전용 변수, 수정 불가능
// data : 자동으로 getter setter 생성, toString 재정의 하지 않아도 Model 클래스로 사용할 수 있다.
data class Todo(val text: String, var isDone: Boolean = false)

// List를 받은 Adapter
//input이 있고 아웃풋이 없는 onClickDeleteIcon 함수를 받음
class TodoAdapter(
    private var myDataset: List<DocumentSnapshot>,
    val onClickDeleteIcon: (todo: DocumentSnapshot) -> Unit,
    val onClickItem: (todo: DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    // 모든 바인딩 객체는 본인이 어떤 view를 위한 것인지 root변수로 가지고 있다
    class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(ItemTodoBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = myDataset[position]
        holder.binding.tvItem.text = todo.getString("text") ?: ""

        // apply쓰면 this로 객체가 넘어감
        holder.binding.tvItem.apply {
            if (todo.getBoolean("done") ?: false) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setTypeface(null, Typeface.ITALIC)
            }
            else {
                paintFlags = 0
                setTypeface(null, Typeface.NORMAL)
            }
        }

        holder.binding.ivDelete.setOnClickListener {
            onClickDeleteIcon.invoke(todo)
        }

        holder.binding.root.setOnClickListener {
            onClickItem.invoke(todo)
        }
    }

    override fun getItemCount(): Int = myDataset.size

    fun setData(newData: List<DocumentSnapshot>) {
        myDataset = newData
        notifyDataSetChanged()
    }
}


// View Binding 사용하지 않을 때 일반 Adapter
//// val : 읽기 전용 변수, 수정 불가능
//// data : 자동으로 getter setter 생성, toString 재정의 하지 않아도 Model 클래스로 사용할 수 있다.
//data class Todo(val text: String, var isDone: Boolean)
//
//// List를 받은 Adapter
//class TodoAdapter(private val myDataset: List<Todo>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
//    class TodoViewHolder(val view: View) : RecyclerView.ViewHolder(view)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
//        return TodoViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
//
//        //findViewById를 사용하지 않게 view binding이라는 걸 사용한다.
//        val textView = holder.view.findViewById<TextView>(R.id.tv_item)
//        textView.text = myDataset[position].text
//    }
//
//    override fun getItemCount(): Int = myDataset.size
//}



// Firebase가 뭐냐
// Firebase는 고품질 앱을 개발하고, 사용자층을 늘리고, 더 많은 수익을 창출하는 데 기여하는 도구를 제공합니다
// 기초적인 기능이 간단히 해결되므로 개발자는 비즈니스에서 수익을 창출하고 사용자에게 집중할 수 있습니다
// 개발에 집중하게 하고 나머지 기능은 이쪽에서 집중하게 하겠다는 뜻
// 앱을 개발하고 이걸 비즈니스적으로 확장을 하고 싶어도(서버나 그런 지식이 없어서 못하게 되더라도) Firabase가 그걸 도와준다라는거죠

//세가지 묶음(앱 빌드, 앱 품질 향상, 비즈니스 성장 도모)
// 앱 빌드(Cloud FireStore, 실시간 데이터베이스) 둘다 데이터 베이스임 Firestore를 많이 쓰는 추세 (NoSQL클라우드 데이터베이스 - 비관계형 데이터베이스, 문서기반으로 저장)
// 많은 사용자의 데이터를 업데이트 하기 위해서는 Firestore하나로 부족하고 'Firebase 인증'을 볼것임(사용자마다 각자 저장소를 갖게)
// 앱 품질 향상(Crashlytics, Test Lab)
// 비즈니스 성장 도모(애널리틱스, 클라우드 메시징)
// google-services.json (Firebase에 대한 정보가 담겨있는 파일, 외부에 노출되면 안됨)