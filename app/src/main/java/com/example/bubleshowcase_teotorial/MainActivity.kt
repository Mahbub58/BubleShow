package com.example.bubleshowcase_teotorial

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.instaal.bubbleshow.BubbleShowCaseBuilder
import io.instaal.bubbleshow.BubbleShowCaseSequence

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bubbleShowCaseBuilder1 = BubbleShowCaseBuilder(this) //Activity instance

        bubbleShowCaseBuilder1.title("Lorem ipsum") //Any title for the bubble view
            .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
            .backgroundColor(getColor(R.color.white))
            .textColor(R.color.black)
            .targetView(findViewById(R.id.home)) //View to point out


        //                .show(); //Display the ShowCase
        val bubbleShowCaseBuilder2 = BubbleShowCaseBuilder(this) //Activity instance

        bubbleShowCaseBuilder2.title("Lorem ipsum") //Any title for the bubble view
            .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
            .textColor(R.color.black)
            .closeActionImageResourceId(R.drawable.ic_baseline_close_24)
            .targetView(findViewById(R.id.home2)) //View to point out

//                .show(); //Display the ShowCase

        //                .show(); //Display the ShowCase
        val bubbleShowCaseBuilder3 = BubbleShowCaseBuilder(this) //Activity instance

        bubbleShowCaseBuilder3.title("Lorem ipsum") //Any title for the bubble view
            .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
            .backgroundColor(getColor(R.color.teal_200))
            .textColor(R.color.black)
            .targetView(findViewById(R.id.home3)) //View to point out


        BubbleShowCaseSequence()
            .addShowCase(bubbleShowCaseBuilder1) //First BubbleShowCase to show
            .addShowCase(bubbleShowCaseBuilder2) // This one will be showed when firstShowCase is dismissed
            .addShowCase(bubbleShowCaseBuilder3) // This one will be showed when secondShowCase is dismissed
            .show() //Display the ShowCaseSequence


    }
}