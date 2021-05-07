package com.ludoven.touchpull.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.animation.PathInterpolatorCompat
import com.ludoven.touchpull.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

/**
 *    author : ludoven
 *    github : https://github.com/ludoven
 *    date   : 2021/5/6  15:10
 */
open class TouchPullView : View {
    //圆的画笔
    private lateinit var mCirclePaint: Paint

    //圆的半径
    private var mCircleRadius = 40f

    private var mCirclePointX = 0f
    private var mCirclePointY = 0f

    //可拖动的高度
    private var mDragHeight = 300

    //进度值
    private var mProgress = 0f

    //目标宽度
    private var mTargetWidth = 400

    //贝塞尔曲线的路径以及画笔
    private var mPath = Path()
    private var mPathPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    //重心点最终高度，决定控制点的Y坐标
    private var mTargetGravityHeight = 10

    //角度变换 0-135°
    private var mTangentAngle = 105

    private var mContent: Drawable?=null
    private var mContentMargin=0
    private var mColor=Color.GRAY

    var mProgressInterpolator=DecelerateInterpolator()
    var mAngleInterpolator:Interpolator?=null

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    /**
     * 当进行测量时触发
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 宽度的意图，宽度的类型
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)

        // 高度的意图,高度的类型
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        //最小的宽度
        val iWidth = (2 * mCircleRadius + paddingLeft + paddingRight).toInt()
        val iHeight = ((mDragHeight * mProgress + 0.5f).toInt()
                + paddingTop + paddingBottom)

        val measureWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                //确切的值
                width
            }
            MeasureSpec.AT_MOST -> {
                //最多
                min(iWidth, width)
            }
            else -> {
                //未知的
                iWidth
            }
        }

        val measureHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                //确切的值
                height
            }
            MeasureSpec.AT_MOST -> {
                //最多
                min(iHeight, height)
            }
            else -> {
                //未知的
                iHeight
            }
        }
        //设置测量的高度宽度
        setMeasuredDimension(measureWidth, measureHeight)
    }

    /**
     *  当大小改变时触发
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        /*mCirclePointX =(width/2).toFloat()
        mCirclePointY=(height/2).toFloat()*/
        //当高度变化时进行路径更新
        updatePathLayout()
    }


    @SuppressLint("Recycle")
    private fun init(attrs: AttributeSet?) {

        initAttrs(attrs)

        var p = Paint(Paint.ANTI_ALIAS_FLAG)
        //设置抗锯齿
        p.isAntiAlias = true
        //设置防抖动
        p.isDither = true
        //设置为填充方式
        p.style = Paint.Style.FILL
        p.color = mColor
        mCirclePaint = p

        //初始化路径部分画笔
        p = Paint(Paint.ANTI_ALIAS_FLAG)
        //设置抗锯齿
        p.isAntiAlias = true
        //设置防抖动
        p.isDither = true
        //设置为填充方式
        p.style = Paint.Style.FILL
        p.color = mColor
        mPathPaint = p

        //切角路径插值器
        mAngleInterpolator=PathInterpolatorCompat.create(
            (mCircleRadius*2.0f)/mDragHeight,90f/mTangentAngle
        )
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val array=context.obtainStyledAttributes(attrs
            ,R.styleable.TouchPullView,0,0)
        mColor=array.getColor(R.styleable.TouchPullView_pColor,Color.DKGRAY)
        mCircleRadius=array.getDimension(R.styleable.TouchPullView_pRadius,mCircleRadius)
        mDragHeight=array.getDimensionPixelOffset(R.styleable.TouchPullView_pDragHeight,mDragHeight)
        mTangentAngle=array.getInteger(R.styleable.TouchPullView_pTangentAngle,mTangentAngle)
        mTargetWidth=array.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetWidth,mTargetWidth)
        mTargetGravityHeight=array.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetGravityHeight,mTargetGravityHeight)

        mContent=array.getDrawable(R.styleable.TouchPullView_pContentDrawable)
        mContentMargin=array.getDimensionPixelOffset(R.styleable.TouchPullView_pContentDrawableMargin,0)

        //销毁
        array.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //进行基础坐标参数改变
        val count = canvas?.save()
        val tranX = (width - getValueByLine(
            width.toFloat()
            , mTargetWidth.toFloat(), mProgress
        )) / 2

        canvas?.translate(tranX, 0f)

        //画贝塞尔曲线
        mPath.let { canvas?.drawPath(it, mPathPaint) }

        //画圆
        canvas?.drawCircle(mCirclePointX, mCirclePointY, mCircleRadius, mCirclePaint)

        val drawable=mContent
        if (drawable!=null){
            canvas?.save()
            //剪切矩形区域
            canvas?.clipRect(drawable.bounds)
            //绘制drawable
            drawable.draw(canvas!!)
            canvas.restore()
        }

        count?.let { canvas.restoreToCount(it) }
    }

    fun setProgress(progress: Float) {
        mProgress = progress
        //请求重新进行测量
        requestLayout()
    }

    /**
     *  更新路径等相关操作
     */
    private fun updatePathLayout() {
        val progress = mProgressInterpolator.getInterpolation(mProgress)

        //可绘制区域的高度宽度
        val w = getValueByLine(width.toFloat(), mTargetWidth.toFloat(), mProgress)
        val h = getValueByLine(0f, mDragHeight.toFloat(), mProgress)
        //x对称轴的参数
        val cPointX = w / 2.0f
        //圆的半径
        val cRadius = mCircleRadius
        //圆心Y坐标
        val cPointY = h - cRadius
        //控制点结束Y的值
        val endControlY = mTargetGravityHeight.toFloat()

        //更新圆的坐标
        mCirclePointX = cPointX
        mCirclePointY = cPointY

        val path = mPath
        //重置
        path.reset()
        path.moveTo(0f, 0f)

        //左边的部分结束点和控制点
        var lEndPointX = 0f
        var lEndPointY = 0f
        var lControlPointX = 0f
        var lControlPointY = 0f


        //获取当前切线的弧度
        val angle=mTangentAngle* mAngleInterpolator?.getInterpolation(progress)!!
        val  radian=Math.toRadians(angle.toDouble())
        val x = (sin(radian) * cRadius).toFloat()
        val y = (cos(radian) * cRadius).toFloat()

        lEndPointX = cPointX - x
        lEndPointY = cPointY + y
        //控制点Y坐标的变化
        lControlPointY = getValueByLine(0f, endControlY, progress)
        //控制点与结束点之间的高度
        val tHeight = lEndPointY - lControlPointY
        //控制点与X的坐标距离
        val tWidth = (tHeight / tan(radian)).toFloat()
        lControlPointX = lEndPointX - tWidth

        //贝塞尔曲线
        path.quadTo(lControlPointX, lControlPointY, lEndPointX, lEndPointY)
        //连接到右边
        path.lineTo(cPointX + (cPointX - lEndPointX), lEndPointY)
        //右边的贝塞尔曲线
        path.quadTo(cPointX + cPointX - lControlPointX, lControlPointY, w, 0f)

        //更新内容部分Drawable
        updateContentLayout(cPointX,cPointY,cRadius)
    }

    /**
     * 对内容部分进行测量并设置
     * @param cx 圆心X
     * @param cy 圆心Y
     * @param radius 半径
     */
    private fun updateContentLayout(cx:Float,cy:Float,radius:Float){
        val drawable =mContent
        if (drawable!=null){
            val margin=mContentMargin
            val l=(cx-radius+margin).toInt()
            val r=(cx+radius-margin).toInt()
            val t=(cy-radius+margin).toInt()
            val b=(cy+radius-margin).toInt()
            drawable.setBounds(l,t,r,b)
        }
    }

    /**
     *  获取当前值
     *  @param start 起始值
     *  @param end  结束值
     * @param progress  进度
     */
    private fun getValueByLine(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }

    //释放动画
    private var valueAnimator: ValueAnimator? = null

    /**
     * 添加释放操作
     */
    open fun release() {
        if (valueAnimator == null) {
            val animator = ValueAnimator.ofFloat(mProgress, 0f)
            animator.interpolator = DecelerateInterpolator()
            animator.duration = 400
            animator.addUpdateListener {
                val ob = animator.animatedValue
                if (ob is Float) {
                    setProgress(ob)
                }
            }
            valueAnimator = animator
        } else {
            valueAnimator?.cancel()
            valueAnimator?.setFloatValues(mProgress, 0f)
        }
        valueAnimator?.start()
    }
}