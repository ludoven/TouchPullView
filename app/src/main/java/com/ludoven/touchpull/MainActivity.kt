package com.ludoven.touchpull

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.ludoven.touchpull.widget.TouchPullView

class MainActivity : AppCompatActivity(), View.OnTouchListener {
    companion object{
       const val TOUCH_MOVE_MAX_Y=600f
    }

    private lateinit var mainLayout:ConstraintLayout
    private lateinit var touchPullView: TouchPullView

    private var mTouchMoveStartY=0f



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout=findViewById(R.id.mainLayout)
        mainLayout.setOnTouchListener(this)
        touchPullView=findViewById(R.id.touchPull)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        //得到意图
        when(event?.actionMasked){
            MotionEvent.ACTION_DOWN->{
                mTouchMoveStartY= event.y
                return true
            }
            MotionEvent.ACTION_MOVE->{
                val y=event.y
                //判断是否往下拉
                if (y>=mTouchMoveStartY){
                    val moveSize=y-mTouchMoveStartY
                    val progress=if (moveSize>= TOUCH_MOVE_MAX_Y) 1f
                    else  moveSize/ TOUCH_MOVE_MAX_Y
                    touchPullView.setProgress(progress)
                }
                return true
            }
            MotionEvent.ACTION_UP->{
                touchPullView.release()
                return true
            }
        }
        return false
    }

    /**
     * 设置进度
     */
    fun setProgress(progress:Float){

    }
}