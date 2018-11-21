# ViewModelFactory
#### Automatically generate factories for (Android Architecture Component's) ViewModels to enable easy assisted dependency injection

This is an annotation processing library that generates a `ViewModelProvider.Factory`s for each `ViewModel` that allow easy and clean assisted dependency injection by any JSR-330-compatible library, like Dagger. 
This means that any `ViewModel` can be provided with both **common dependencies** _and_ **variable parameters** in the same constructor.

```
@ViewModelFactory
class SampleViewModel(@Provided private val repository: Repository, private val userId: Int) : ViewModel() {
    private val greeting = MutableLiveData<String>()

    init {
        val user = repository.getUser(userId)
        greeting.value = "Hello, $user.name"
    }

    fun getGreeting() = greeting as LiveData<String>
}
```
```
class SampleActivity : AppCompatActivity() {
    @Inject
    lateinit var sampleViewModelFactory2: SampleViewModelFactory2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        appComponent.inject(this)

        val userId = intent.getIntExtra("USER_ID", -1)
        val viewModel = ViewModelProviders.of(this, sampleViewModelFactory2.create(userId))
            .get(SampleViewModel::class.java)

        viewModel.getGreeting().observe(this, Observer { greeting ->
            greetingTextView.text = greeting
        })
    }
}
```

- `@ViewModelFactory` - Annotate either the `ViewModel` class or the specific constructor to be used by the factory
- `@Provided` - Annotate all constructor arguments that can be provided by the graph of the dependency injection library
- The rest of the variable constructor arguments will be taken by the factory in the `create()` method
- You can inject the generated factory in your `Activity` or `Fragment` and use it to effortlessly construct dependency-satisfied and parametrised `ViewModel`s

## Installation
In your `app.gradle` file:
```
apply plugin: 'kotlin-kapt'
// The following is currently needed with kapt when a library uses other generated code in its own annotation processing step 
// (i.e. Dagger using ViewModelFactory generated classes to complete its dependency graph)
kapt {
    correctErrorTypes = true
}
    
implementation "com.radutopor.viewmodelfactory:annotations:1.0.0"
kapt "com.radutopor.viewmodelfactory:processor:1.0.0"       // use `annotationProcessor` for Java
```
