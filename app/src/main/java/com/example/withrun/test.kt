package com.example.withrun

//fun main() {
//    println("What's your name?")
//    val name= readLine()
//    println("Hello $name!")
//}

    val hello1 = "hello world" //상수
    var hello2:  String = "hello2"  // null   - String hello2 = null 이 안됨
    var hello3:  String? = "hello3"  // null 을 넣을 수 없음


fun main() {
    // 배열
    var arr1 = listOf("1","2")  // 넣고(add) 빼고 수정이 안됨
    var arr2 = mutableListOf("1","2")  // 넣고(add) 빼고 수정이 가능

    // 반복문 (향상된 반복문)
    for(item in arr1) { // 안에있는 내용 in 배열명
        println(item)
    }
    for((index, item) in arr1.withIndex()) { // 인덱스넘버까지 조회
        println("$index, $item")
    }
    //캐스팅 casting object < String, int, long ..... // Any - auto casting
    var hello: Any = "hello" // Any 라고 선언했지만 String인 변수
    if(hello is String) { // hello 라는 변수가 string 이면
        var str: String = hello

        // 자바에선 (String) hello;  이런식으로 캐스팅이 필요함
    }

    // 클래스 호출
    var cls = HelloClass()
    var cls2 = HelloClass(1)
    println(cls2.age)
    var person = Person(1, "anna")
    println(person.name)

    // map
    var map1 = mapOf(Pair("name","anna")) // 수정이 불가능
    var map2 = mutableMapOf<String, String>() // 수정이 가능
    map2.put("name","santi")
    print(map2.keys) // map2안에 key 만 배열로 return
    for(map in map2) {
        println(map.value) // map2안에 value만 return
    }

}

// class - 자동차 (시동, 운전), 사람(밥먹는다, 걷는다), 동물(뛴다)
class HelloClass {
    var age: Int = 0;
    init {
    }

    //기본생성자, 보조생성자
    constructor() // 기본 생성자
    constructor(age: Int) { //  보조생성자 set
        this.age = age
    }
}

// data class - set, get
data class Person(var age: Int, val name: String)
