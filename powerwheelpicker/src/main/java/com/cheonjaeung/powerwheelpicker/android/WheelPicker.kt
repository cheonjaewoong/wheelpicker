package com.cheonjaeung.powerwheelpicker.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * [WheelPicker] is a view that allow user to select one item from multiple choices.
 *
 * [WheelPicker] needs some components to work correctly.
 *
 * - Adapter: An adapter implements [RecyclerView.Adapter] to handle items.
 * This view is implemented on top of the [RecyclerView]. Set adapter into [adapter] property or `setAdapter`
 * method in Java.
 * - Selector Size: A pixel size of the selector at center of the picker. The selector must be greater than 0.
 * The selector is also used for selection detecting. When an item is positioned within the selector,
 * selected callback will be triggered. Set selector size into [selectorWidth] and [selectorHeight] or
 * set `selector_width` and `selector_height` property in the XML.
 */
class WheelPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * The underlying [RecyclerView].
     */
    private val recyclerView: RecyclerView

    /**
     * The underlying layout manager for [recyclerView].
     */
    private val layoutManager: PickerLayoutManager

    /**
     * Reusable instance to layout internal views.
     */
    private val layoutRect: Rect = Rect()

    /**
     * A [RecyclerView.Adapter] to provide picker items on demand.
     */
    var adapter: RecyclerView.Adapter<*>?
        get() = recyclerView.adapter
        set(value) {
            recyclerView.adapter = value
        }

    /**
     * Current orientation of this picker, Either [HORIZONTAL] or [VERTICAL].
     */
    @RecyclerView.Orientation
    var orientation: Int
        get() = layoutManager.orientation
        set(value) {
            if (value != HORIZONTAL && value != VERTICAL) {
                throw IllegalArgumentException("Invalid orientation: $value")
            }
            layoutManager.orientation = value
        }

    /**
     * Enable circular mode which means that the first/last item will be connected to the last/first.
     */
    var circular: Boolean
        get() = layoutManager.circular
        set(value) {
            layoutManager.circular = value
        }

    /**
     * A pixel width of the selector area in the picker. The size must be a positive.
     */
    var selectorWidth: Int = 0
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("selectorWidth must be a positive value")
            }
            field = value
        }

    /**
     * A pixel height of the selector area in the picker. The size must be a positive.
     */
    var selectorHeight: Int = 0
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("selectorHeight must be a positive value")
            }
            field = value
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker)
        ViewCompat.saveAttributeDataForStyleable(
            this,
            context,
            R.styleable.WheelPicker,
            attrs,
            a,
            defStyleAttr,
            defStyleRes
        )

        val orientation = a.getInt(R.styleable.WheelPicker_android_orientation, DEFAULT_ORIENTATION)
        val circular = a.getBoolean(R.styleable.WheelPicker_circular, DEFAULT_CIRCULAR)
        selectorWidth = a.getDimensionPixelSize(R.styleable.WheelPicker_selector_width, 0)
        selectorHeight = a.getDimensionPixelSize(R.styleable.WheelPicker_selector_height, 0)
        a.recycle()

        recyclerView = RecyclerView(context)
        layoutManager = PickerLayoutManager(orientation, circular)
        recyclerView.layoutManager = layoutManager
        recyclerView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        recyclerView.clipToPadding = false
        val snapHelper = PickerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        attachViewToParent(recyclerView, 0, recyclerView.layoutParams)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(recyclerView, widthMeasureSpec, heightMeasureSpec)

        val width = recyclerView.measuredWidth
        val height = recyclerView.measuredHeight
        val measuredState = recyclerView.measuredState

        setMeasuredDimension(
            resolveSizeAndState(width, widthMeasureSpec, measuredState),
            resolveSizeAndState(
                height,
                heightMeasureSpec,
                measuredState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutRect.set(
            paddingLeft,
            paddingTop,
            right - left - paddingRight,
            bottom - top - paddingBottom
        )

        @SuppressLint("WrongConstant")
        when (orientation) {
            HORIZONTAL -> {
                val innerPadding = (measuredWidth / 2) - (selectorWidth / 2)
                recyclerView.setPadding(innerPadding, 0, innerPadding, 0)
                if (selectorWidth == 0) {
                    Log.w(TAG, "selectorWidth should be set bigger than 0")
                }
            }

            VERTICAL -> {
                val innerPadding = (measuredHeight / 2) - (selectorHeight / 2)
                recyclerView.setPadding(0, innerPadding, 0, innerPadding)
                if (selectorHeight == 0) {
                    Log.w(TAG, "selectorHeight should be set bigger than 0")
                }
            }
        }

        recyclerView.layout(layoutRect.left, layoutRect.top, layoutRect.right, layoutRect.bottom)
    }

    companion object {
        private const val TAG: String = "WheelPicker"

        const val HORIZONTAL: Int = RecyclerView.HORIZONTAL
        const val VERTICAL: Int = RecyclerView.VERTICAL

        private const val DEFAULT_ORIENTATION: Int = VERTICAL
        private const val DEFAULT_CIRCULAR: Boolean = true
    }
}