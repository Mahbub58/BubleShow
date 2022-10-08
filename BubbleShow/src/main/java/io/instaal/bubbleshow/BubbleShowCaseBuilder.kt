package io.instaal.bubbleshow

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference
import java.util.ArrayList

class BubbleShowCaseBuilder{

    internal var mActivity: WeakReference<Activity>? = null
    internal var mImage: Drawable? = null
    internal var mTitle: String? = null
    internal var mSubtitle: String? = null
    internal var mCloseAction: Drawable? = null
    internal var mBackgroundColor: Int? = null
    internal var mTextColor: Int? = null
    internal var mTitleTextSize: Int? = null
    internal var mSubtitleTextSize: Int? = null
    internal var mHighlightMode: ShowCase.HighlightMode? = null
    internal var mDisableTargetClick: Boolean = false
    internal var mDisableCloseAction: Boolean = false
    internal var mShowOnce: String? = null
    internal var mIsFirstOfSequence: Boolean? = null
    internal var mIsLastOfSequence: Boolean? = null
    internal val mArrowPositionList = ArrayList<ShowCase.ArrowPosition>()
    internal var mTargetView: WeakReference<View>? = null
    internal var mShowCaseListener: ShowCaseListener? = null
    internal var mSequenceShowCaseListener: SequenceShowCaseListener? = null

    private var onGlobalLayoutListenerTargetView: ViewTreeObserver.OnGlobalLayoutListener? = null


    constructor(activity: Activity){
        mActivity = WeakReference(activity)
    }


    fun title(title: String): BubbleShowCaseBuilder {
        mTitle = title
        return this
    }


    fun description(subtitle: String): BubbleShowCaseBuilder {
        mSubtitle = subtitle
        return this
    }


    fun image(image: Drawable): BubbleShowCaseBuilder {
        mImage = image
        return this
    }


    fun imageResourceId(resId: Int): BubbleShowCaseBuilder {
        mImage = ContextCompat.getDrawable(mActivity!!.get()!!, resId)
        return this
    }


    fun closeActionImage(image: Drawable?): BubbleShowCaseBuilder {
        mCloseAction = image
        return this
    }




    fun textColor(color: Int): BubbleShowCaseBuilder {
        mTextColor = color
        return this
    }


    fun textColorResourceId(colorResId: Int): BubbleShowCaseBuilder {
        mTextColor = ContextCompat.getColor(mActivity!!.get()!!, colorResId)
        return this
    }


    fun titleTextSize(textSize: Int): BubbleShowCaseBuilder {
        mTitleTextSize = textSize
        return this
    }

    fun closeActionImageResourceId(resId: Int): BubbleShowCaseBuilder {
        mCloseAction = ContextCompat.getDrawable(mActivity!!.get()!!, resId)
        return this
    }


    fun backgroundColor(color: Int): BubbleShowCaseBuilder {
        mBackgroundColor = color
        return this
    }

    fun backgroundColorResourceId(colorResId: Int): BubbleShowCaseBuilder {
        mBackgroundColor = ContextCompat.getColor(mActivity!!.get()!!, colorResId)
        return this
    }


    fun descriptionTextSize(textSize: Int): BubbleShowCaseBuilder {
        mSubtitleTextSize = textSize
        return this
    }





    fun disableTargetClick(isDisabled: Boolean): BubbleShowCaseBuilder{
        mDisableTargetClick = isDisabled
        return this
    }


    fun disableCloseAction(isDisabled: Boolean): BubbleShowCaseBuilder{
        mDisableCloseAction = isDisabled
        return this
    }


    fun arrowPosition(arrowPosition: ShowCase.ArrowPosition): BubbleShowCaseBuilder {
        mArrowPositionList.clear()
        mArrowPositionList.add(arrowPosition)
        return this
    }

    fun showOnce(id: String): BubbleShowCaseBuilder {
        mShowOnce = id
        return this
    }


    fun targetView(targetView: View): BubbleShowCaseBuilder {
        mTargetView = WeakReference(targetView)
        return this
    }

    fun arrowPosition(arrowPosition: List<ShowCase.ArrowPosition>): BubbleShowCaseBuilder {
        mArrowPositionList.clear()
        mArrowPositionList.addAll(arrowPosition)
        return this
    }


    fun highlightMode(highlightMode: ShowCase.HighlightMode): BubbleShowCaseBuilder {
        mHighlightMode = highlightMode
        return this
    }

    fun listener(showCaseListener: ShowCaseListener): BubbleShowCaseBuilder {
        mShowCaseListener = showCaseListener
        return this
    }

    internal fun sequenceListener(sequenceShowCaseListener: SequenceShowCaseListener): BubbleShowCaseBuilder {
        mSequenceShowCaseListener = sequenceShowCaseListener
        return this
    }

    internal fun isFirstOfSequence(isFirst: Boolean): BubbleShowCaseBuilder{
        mIsFirstOfSequence = isFirst
        return this
    }

    internal fun isLastOfSequence(isLast: Boolean): BubbleShowCaseBuilder{
        mIsLastOfSequence = isLast
        return this
    }


    private fun build(): ShowCase {
        if(mIsFirstOfSequence ==null)
            mIsFirstOfSequence = true
        if(mIsLastOfSequence ==null)
            mIsLastOfSequence = true

        return ShowCase(this)
    }


    fun show(): ShowCase{
        val bubbleShowCase = build()
        if (mTargetView != null) {
            val targetView = mTargetView!!.get()
            if (targetView!!.height == 0 || targetView.width == 0) {
                //If the view is not already painted, we wait for it waiting for view changes using OnGlobalLayoutListener
                onGlobalLayoutListenerTargetView = ViewTreeObserver.OnGlobalLayoutListener {
                    bubbleShowCase.show()
                    targetView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListenerTargetView)
                }
                targetView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListenerTargetView)
            } else {
                bubbleShowCase.show()
            }
        } else {
            bubbleShowCase.show()
        }
        return bubbleShowCase
    }

}