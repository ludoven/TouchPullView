package com.ludoven.touchpull.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 *
 *    贝塞尔曲线
 *    author : ludoven
 *    github : https://github.com/ludoven
 *    date   : 2021/5/6  16:53
 */
class BezierView :View{
    constructor(context: Context?) : super(context){
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes){
        init()
    }

    private var mPaint:Paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPath:Path= Path()
    private var mBezierPath:Path= Path()
    private var mSrcBezier:Path= Path()

    /**
     *  一个成员变量用上了三次或者三次以上
     *    建议先写一个局部变量赋值过去
     */
    private fun init() {
        val paint=mPaint
        //设置抗锯齿
        paint.isAntiAlias = true
        //设置防抖动
        paint.isDither = true
        //设置为填充方式
        paint.style = Paint.Style.STROKE
        paint.strokeWidth =10f
        paint.color = Color.CYAN


        //初始化源贝塞尔曲线
        mSrcBezier.cubicTo(200f,700f,500f,1200f,700f,200f)

        object : Thread() {
            override fun run() {
                super.run()
                initBezier()
            }
        }.start()

    }

    /**
     *  初始化1到3阶贝塞尔曲线
     */
    private fun initTestBezier() {
        //一阶贝塞尔曲线
        val path= mPath
        path.moveTo(100f,100f)
        path.lineTo(300f,300f)

        //二阶贝塞尔曲线
        // path.quadTo(500f,0f,700f,300f)
        //相对上一次结束的位置
        path.rQuadTo(200f,-300f,400f,0f)

        path.moveTo(400f,800f)
        //三阶贝塞尔曲线
        //path.cubicTo(500f,600f,700f,1000f,800f,800f)
        path.rCubicTo(100f,-200f,300f,400f,400f,0f)
    }
    /**
     *  初始化贝塞尔曲线4阶以上
     */
    private fun initBezier(){
        val xPoints= floatArrayOf(0f,200f,500f,700f,800f)
        val yPoints= floatArrayOf(0f,700f,1200f,200f,800f)

        val path =mBezierPath

        val fps=20000
        for (i in 0..fps){
            //进度
            val progress=(i/fps.toFloat())
            val x=calculateBezier(progress,xPoints)
            val y=calculateBezier(progress,yPoints)
            //使用连接的方式，当XY变动足够小的情况下，就是平滑曲线
            path.lineTo(x,y)

            postInvalidate()
            try {
                Thread.sleep(10)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    /**
     *  计算某时刻的贝塞尔所处的值(x或y)
     *  @param t 时间 (0-1)
     *  @param values 贝塞尔点集合 ( x 或 y)
     *  @return  当前t时刻的贝塞尔所处点
     */
    private  fun calculateBezier(t: Float,  values: FloatArray): Float {
        //采用双层循环
        val len = values.size
        for (i in len - 1 downTo 1) {
            for (j in 0 until i) {
                //计算
                values[j] = values[j] + (values[j + 1] - values[j]) * t
            }
        }
        //运算时结构保存在第一位
        //所以返回第一位
        return values[0]
    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mPaint.color=Color.GRAY
        canvas?.drawPath(mSrcBezier, mPaint)
        mPaint.color=Color.RED
        canvas?.drawPath(mBezierPath, mPaint)
    }
}