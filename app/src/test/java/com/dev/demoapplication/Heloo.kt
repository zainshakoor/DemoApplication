package com.dev.demoapplication


/*
class DebugExample(private val name: String, private var count: Int) {

    // Method to increment the count
    fun increment() {
        count++
        println("Incremented count to: $count")
    }

    // Method to reset the count
    fun reset() {
        count = 0
        println("Count has been reset.")
    }

    // Method to display the current state
    fun displayState() {
        println("Name: $name, Count: $count")
    }


    fun placeOrder(foodItem: String, callback: OrderCallback) {
        println("Order Placed for: $foodItem")
        println("Order in Procssing")
        Thread {
            Thread.sleep(3000)
            callback.onOrderReady("Your order for $foodItem is ready")
        }
    }
}
*/
class FoodDeliverySystem{
    fun placeOrder(foodItem: String,callback: OrderCallback){
        println("Order Placed for: $foodItem")
        print("Order in processing....")
        Thread{
            Thread.sleep(4000)
            callback.onOrderReady("Your order for $foodItem is ready ")
        }.start()
    }
}
 interface OrderCallback {
    fun onOrderReady(orderDetails: String)
}

fun main() {
//    val debugObj = DebugExample("Demo", 5)
    val foodDelieverySystem =FoodDeliverySystem()
    foodDelieverySystem.placeOrder("Pizza",object :OrderCallback{
        override fun onOrderReady(orderDetails: String) {
            println(orderDetails) // Here, you implement the method

        }

    }
    )

    // Debugging step-by-step
  /*  debugObj.displayState() // Observe initial state
    debugObj.increment()    // Increment count
    debugObj.displayState() // Observe updated state
    debugObj.reset()        // Reset count
    debugObj.displayState() // Observe final state*/
}
