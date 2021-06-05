package com.jclian.sudoku.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jclian.sudoku.BuildConfig.DEBUG
import com.jclian.sudoku.R
import com.jclian.sudoku.Sudoku
import kotlin.math.min
import kotlin.math.pow


class SudokuView(
    context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : View(context, attrs, defStyleAttr) {

    private var hadDrawingLines: Boolean = false
    private var highLightTextColor = Color.parseColor("#191919")
    private var fillTextColor = Color.parseColor("#d6d6d6")
    private var pinedTextColor = Color.parseColor("#a0a0a0")
    private var selectedCircleColor = Color.parseColor("#9c915d")
    private var pinedCircleColor = Color.parseColor("#2d2d2d")
    private var highlightCircleColor = Color.parseColor("#666355")
    private var yBtmFun: Float = -1f
    private var wBtmFun: Float = -1f
    private var menuNum: Int = -1
    private var menuCircleRadius: Float = 0f
    private var wMenu: Float = 0f

    // 记录选中九宫数字的坐标
    private var posY: Int = -1
    private var posX: Int = -1

    private val paint: Paint by lazy { Paint() }

    private val DEFAULT_SIZE: Int = 450

    private var wBlock: Int = DEFAULT_SIZE - 10 / 10
    private var hBlock: Int = DEFAULT_SIZE - 10 / 10

    private val initData: HashMap<String, Int> by lazy { HashMap<String, Int>() }
    private val fillData: HashMap<String, Int> by lazy { HashMap<String, Int>() }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)


    init {
        val a = context?.obtainStyledAttributes(attrs, R.styleable.SudokuView, defStyleAttr, 0)
        a?.run {
            highLightTextColor = getColor(R.styleable.SudokuView_textColor_highlight, 0x191919)
            pinedTextColor = Color.parseColor(getString(R.styleable.SudokuView_textColor_pined))
            fillTextColor = getColor(R.styleable.SudokuView_textColor_fill, 0xd6d6d6)
        }
        a?.recycle()

        setBackgroundColor(highLightTextColor)
    }

    val Float.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas!!)
        canvas.apply {
            drawLines(this)
            drawCells(this)
            drawNumPad(this)
        }

    }

    /**
     * 画九宫格中数字
     */
    private fun drawCells(canvas: Canvas) {
        val paint = Paint()
        for (i in 0..8) {
            paint.reset()

            for (j in 0..8) {
                val key = "$i,$j"
                val pinNum = initData.contains(key)
                val fillNum = fillData.contains(key)
                val isSelected = posX == i && posY == j
                if (isSelected) {
                    paint.color = selectedCircleColor
                } else {
                    paint.color = pinedCircleColor

                }
                paint.style = Paint.Style.FILL_AND_STROKE
                val x = i * width / 9f + width / 18f
                val y = j * width / 9f + width / 18f
                val left = i * width / 9f
                val top = j * width / 9f

                if (pinNum || (i == posX && j == posY)) {
                    canvas.drawCircle(x, y, wBlock * 0.8f / 2, paint)
                }
                if (pinNum) {
                    canvas.drawCircle(x, y, wBlock * 0.8f / 2, paint)

                    val num = initData[key]
                    paint.color = pinedTextColor
                    paint.textSize = wBlock * 0.8f / 2
                    paint.textAlign = Paint.Align.CENTER
                    val textHeight: Float = paint.descent() - paint.ascent()
                    val textOffset: Float = textHeight / 2 - paint.descent()
                    val bounds = RectF(left, top, left + wBlock, top + wBlock)
                    canvas.drawText(
                        num.toString(), bounds.centerX(), bounds.centerY() + textOffset, paint
                    )

                }
                val hasFillNum = fillData.contains(key)
                if (hasFillNum) {
                    val num = fillData[key]
                    paint.color = fillTextColor
                    paint.textSize = wBlock * 0.8f / 2
                    paint.textAlign = Paint.Align.CENTER
                    val textHeight: Float = paint.descent() - paint.ascent()
                    val textOffset: Float = textHeight / 2 - paint.descent()
                    val bounds = RectF(left, top, left + wBlock, top + wBlock)
                    canvas.drawText(
                        num.toString(), bounds.centerX(), bounds.centerY() + textOffset, paint
                    )
                }
            }
        }
    }

    /**
     * 画九宫线
     */
    private fun drawLines(canvas: Canvas) {

        val dashPath = Path()
        val paint = Paint()
        paint.color = Color.parseColor("#9c915d")
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE

        val dashPaint = Paint()
        dashPaint.strokeWidth = 2f
        dashPaint.color = Color.parseColor("#2f2e2b")
        dashPaint.style = Paint.Style.STROKE
        dashPaint.pathEffect =
            DashPathEffect(floatArrayOf(wBlock * 0.6f, wBlock * 0.4f), wBlock * -0.2f)

        for (i in 0..8) {
            dashPath.reset()

            val p = if (i == 3 || i == 6) {
                paint
            } else {
                dashPaint
            }
            // 横线
            dashPath.moveTo(0f, (i * hBlock).toFloat())
            dashPath.lineTo(width.toFloat(), (i * hBlock).toFloat())
            canvas.drawPath(dashPath, p)
            // 竖线
            dashPath.moveTo((i * hBlock).toFloat(), 0f)
            dashPath.lineTo((i * hBlock).toFloat(), width.toFloat())
            canvas.drawPath(dashPath, p)

        }
        hadDrawingLines = true

    }


    fun drawNumPad(canvas: Canvas) {
        paint.reset()
        //设定字体大小和对齐方式
        paint.textSize = width / 5f * 0.6f
        paint.textAlign = Paint.Align.CENTER
        // 数字按钮区域
        for (num in 1..10) {
            val text = if (num == 10) {
                "X"
            } else {
                "$num"
            }

            val left = (num - 1) % 5 / 5f * width
            val top = ((num - 1) / 5) * width / 5f + width

            val textHeight: Float = paint.descent() - paint.ascent()
            val textOffset: Float = textHeight / 2 - paint.descent()
            val bounds = RectF(left, top, left + wMenu, top + wMenu)
            if (menuNum == num) {
                paint.color = Color.parseColor("#9c915d")
                paint.style = Paint.Style.FILL_AND_STROKE
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), menuCircleRadius, paint)
                paint.color = Color.parseColor("#FFFFFF")
                canvas.drawText(
                    text,
                    bounds.centerX(),
                    bounds.centerY() + textOffset,
                    paint
                )
            } else {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), menuCircleRadius, paint)
                paint.style = Paint.Style.FILL_AND_STROKE
                canvas.drawText(
                    text,
                    bounds.centerX(),
                    bounds.centerY() + textOffset,
                    paint
                )
            }

        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getMySize(widthMeasureSpec)
        val height = getMySize(heightMeasureSpec)
        Log.d("SudokuView", "width =$width , height = $height")
        setMeasuredDimension(width, height)
        wBlock = width / 9
        hBlock = wBlock
        Log.d("SudokuView", "block width =$wBlock , height = $hBlock")
        wMenu = width / 5f
        menuCircleRadius = wMenu / 2 * 0.8f
        yBtmFun = width + wMenu * 2
        wBtmFun = width / 4f

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_MOVE) {
            return super.onTouchEvent(event)
        }
        // 点击的位置
        if (event?.y!! < width) {
            // 九宫区域
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = (event.x / (width / 9)).toInt()
                val y = (event.y / (width / 9)).toInt()

                val key = "$x,$y"

                val isPin = initData.contains(key)

                if (menuNum != -1) {
                    if (!isPin) {
                        if (fillData.containsKey(key)) {
                            fillData.remove(key)
                        } else {
                            fillData[key] = menuNum
                        }
                    }
                } else {
                    if (x == posX && y == posY) {
                        posX = -1
                        posY = -1
                    } else {
                        posX = x
                        posY = y
                    }
                }

                if (DEBUG) {
                    Log.d("Sudoku", "event = $event, posX = $posX posY = $posY")
                }
            }
        } else if (event.y > width && event.y < width + 2f * wMenu) {
            // 触摸区域在功能菜单
            val y = ((event.y - width) / wMenu).toInt()
            val x = ((event.x) / wMenu).toInt()
            val left = wMenu * x;
            val right = wMenu * (x + 1)
            val top = y * wMenu + width
            val bottom = (y + 1) * wMenu + width
            val rect = RectF(left, top, right, bottom);
            val isInside = ((event.x - rect.centerX()).toDouble()
                .pow(2) + (event.y - rect.centerY()).toDouble()
                .pow(2)) <= menuCircleRadius.pow(2)
            if (isInside) {
                //在圆形菜单内
                val temp = x + 5 * y + 1

                val isPin = initData.contains("$posX,$posY")
                if (posX != -1 && posY != -1) {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        menuNum = if (menuNum == temp || temp == 10) {
                            -1
                        } else {
                            temp
                        }
                        if (!isPin) {
                            val key = "$posX,$posY"
                            if (menuNum == -1 || (fillData.containsKey(key) && fillData[key] == menuNum)) {
                                fillData.remove(key)
                            } else {
                                fillData[key] = temp
                            }
                        }
                    } else if (event.action == MotionEvent.ACTION_UP) {
                        menuNum = -1
                    }
                } else {
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (menuNum == temp) {
                            menuNum = -1
                        } else {
                            menuNum = temp
                        }
                    }
                }

            }
        }
        if (event.action == MotionEvent.ACTION_UP) {
            if ((fillData.size + initData.size) >= 81) {
                val solvedData = HashMap<String, Int>()
                solvedData.putAll(initData)
                solvedData.putAll(fillData)
                if (solvedData.size >= 81) {
                    checkSudoku(solvedData)
                }
            }
        }
        invalidate()
        return true
    }

    private fun checkSudoku(data: HashMap<String, Int>) {
        if (Sudoku.check(data)) {
            Toast.makeText(context, R.string.solved, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, R.string.un_solved, Toast.LENGTH_SHORT).show()

        }
    }

    private fun getMySize(measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.AT_MOST -> min(DEFAULT_SIZE, size)
            MeasureSpec.EXACTLY -> size
            else -> DEFAULT_SIZE
        }
    }

    /**
     *数独初始化
     */
    fun start() {
        val sp = context.getSharedPreferences("dump_sudoku", MODE_PRIVATE)
        val initJson = sp.getString("initdata", null)
        val fillJson = sp.getString("filldata", null)
        initData.clear()
        initData.plusAssign(
            if (initJson.isNullOrBlank()) {
                Sudoku.gen()
            } else {
                val type = object : TypeToken<HashMap<String, Int>>() {}.type
                Gson().fromJson(initJson, type)
            }
        )
        fillData.clear()
        fillData.plusAssign(
            if (fillJson.isNullOrEmpty()) {
                HashMap()
            } else {
                val type = object : TypeToken<HashMap<String, Int>>() {}.type
                Gson().fromJson(fillJson, type)
            }
        )

    }

    /**
     * 缓存数度
     */
    fun dump() {
        val type = object : TypeToken<HashMap<String, Int>>() {}.type
        val puzzleStr = Gson().toJson(initData, type)
        val ansStr = Gson().toJson(fillData, type)
        val sp = context.getSharedPreferences("dump_sudoku", MODE_PRIVATE)
        sp.edit()
            .putString("initdata", puzzleStr)
            .putString("filldata", ansStr)
            .apply()
    }

}