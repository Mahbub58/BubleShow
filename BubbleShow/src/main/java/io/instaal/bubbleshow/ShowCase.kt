package io.instaal.bubbleshow


import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference



class ShowCase(builder: BubbleShowCaseBuilder){
    private val SHARED_PREFS_NAME = "BubbleShowCasePrefs"

    private val FOREGROUND_LAYOUT_ID = 731

    private val DURATION_SHOW_CASE_ANIMATION = 200 //ms
    private val DURATION_BACKGROUND_ANIMATION = 700 //ms
    private val DURATION_BEATING_ANIMATION = 700 //ms

    private val MAX_WIDTH_MESSAGE_VIEW_TABLET = 420 //dp

    /**
     * Enum class which corresponds to each valid position for the BubbleMessageView arrow
     */
    enum class ArrowPosition {
        TOP, BOTTOM, LEFT, RIGHT
    }

    /**
     * Highlight mode. It represents the way that the target view will be highlighted
     * - VIEW_LAYOUT: Default value. All the view box is highlighted (the rectangle where the view is contained). Example: For a TextView, all the element is highlighted (characters and background)
     * - VIEW_SURFACE: Only the view surface is highlighted, but not the background. Example: For a TextView, only the characters will be highlighted
     */
    enum class HighlightMode {
        VIEW_LAYOUT, VIEW_SURFACE
    }


    private val mActivity: WeakReference<Activity> = builder.mActivity!!

    //BubbleMessageView params
    private val mImage: Drawable? = builder.mImage
    private val mTitle: String? = builder.mTitle
    private val mSubtitle: String? = builder.mSubtitle
    private val mCloseAction: Drawable? = builder.mCloseAction
    private val mBackgroundColor: Int? = builder.mBackgroundColor
    private val mTextColor: Int? = builder.mTextColor
    private val mTitleTextSize: Int? = builder.mTitleTextSize
    private val mSubtitleTextSize: Int? = builder.mSubtitleTextSize
    private val mShowOnce: String? = builder.mShowOnce
    private val mDisableTargetClick: Boolean = builder.mDisableTargetClick
    private val mDisableCloseAction: Boolean = builder.mDisableCloseAction
    private val mHighlightMode: ShowCase.HighlightMode? = builder.mHighlightMode
    private val mArrowPositionList: MutableList<ArrowPosition> = builder.mArrowPositionList
    private val mTargetView: WeakReference<View>? = builder.mTargetView
    private val mShowCaseListener: ShowCaseListener?  = builder.mShowCaseListener

    //Sequence params
    private val mSequenceListener: SequenceShowCaseListener?  = builder.mSequenceShowCaseListener
    private val isFirstOfSequence: Boolean  = builder.mIsFirstOfSequence!!
    private val isLastOfSequence: Boolean  = builder.mIsLastOfSequence!!

    //References
    private var backgroundDimLayout: RelativeLayout? = null
    private var messageViewBuilder: MessageView.Builder? = null

    fun show(){
        if(mShowOnce != null){
            if(isBubbleShowCaseHasBeenShowedPreviously(mShowOnce)){
                notifyDismissToSequenceListener()
                return
            } else{
                registerBubbleShowCaseInPreferences(mShowOnce)
            }
        }

        val rootView = getViewRoot(mActivity.get()!!)
        backgroundDimLayout = getBackgroundDimLayout()
        setBackgroundDimListener(backgroundDimLayout)
        messageViewBuilder = getBubbleMessageViewBuilder()

        if (mTargetView != null && mArrowPositionList.size <= 1) {
            //Wait until the end of the layout animation, to avoid problems with pending scrolls or view movements
            val handler = Handler()
            handler.postDelayed({
                val target = mTargetView.get()!!
                //If the arrow list is empty, the arrow position is set by default depending on the targetView position on the screen
                if(mArrowPositionList.isEmpty()){
                    if(Screen.isViewLocatedAtHalfTopOfTheScreen(mActivity.get()!!, target)) mArrowPositionList.add(ArrowPosition.TOP) else mArrowPositionList.add(ArrowPosition.BOTTOM)
                    messageViewBuilder = getBubbleMessageViewBuilder()
                }

                if (isVisibleOnScreen(target)) {
                    addTargetViewAtBackgroundDimLayout(target, backgroundDimLayout)
                    addBubbleMessageViewDependingOnTargetView(target, messageViewBuilder!!, backgroundDimLayout)
                } else {
                    dismiss()
                }
            }, DURATION_BACKGROUND_ANIMATION.toLong())
        } else {
            addBubbleMessageViewOnScreenCenter(messageViewBuilder!!, backgroundDimLayout)
        }
        if(isFirstOfSequence){
            //Add the background dim layout above the root view
            val animation = Animation.getFadeInAnimation(0, DURATION_BACKGROUND_ANIMATION)
            backgroundDimLayout?.let { rootView.addView(Animation.setAnimationToView(backgroundDimLayout!!, animation)) }
        }
    }

    fun dismiss() {
        if (backgroundDimLayout != null && isLastOfSequence) {
            //Remove background dim layout if the BubbleShowCase is the last of the sequence
            finishSequence()
        } else {
            //Remove all the views created over the background dim layout waiting for the next BubbleShowCsse in the sequence
            backgroundDimLayout?.removeAllViews()
        }
        notifyDismissToSequenceListener()
    }

    fun finishSequence() {
        val rootView = getViewRoot(mActivity.get()!!)
        rootView.removeView(backgroundDimLayout)
        backgroundDimLayout = null
    }

    private fun notifyDismissToSequenceListener(){
        mSequenceListener?.let { mSequenceListener.onDismiss() }
    }

    private fun getViewRoot(activity: Activity): ViewGroup {
        val androidContent = activity.findViewById<ViewGroup>(android.R.id.content)
        return androidContent.parent.parent as ViewGroup
    }

    private fun getBackgroundDimLayout(): RelativeLayout {
        if(mActivity.get()!!.findViewById<RelativeLayout>(FOREGROUND_LAYOUT_ID) != null)
            return mActivity.get()!!.findViewById(FOREGROUND_LAYOUT_ID)
        val backgroundLayout = RelativeLayout(mActivity.get()!!)
        backgroundLayout.id = FOREGROUND_LAYOUT_ID
        backgroundLayout.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        backgroundLayout.setBackgroundColor(ContextCompat.getColor(mActivity.get()!!, R.color.transparent_grey))
        backgroundLayout.isClickable = true
        return backgroundLayout
    }

    private fun setBackgroundDimListener(backgroundDimLayout: RelativeLayout?){
        backgroundDimLayout?.setOnClickListener { mShowCaseListener?.onBackgroundDimClick(this) }
    }


    private fun isBubbleShowCaseHasBeenShowedPreviously(id: String): Boolean{
        val mPrefs = mActivity.get()!!.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        return getString(mPrefs, id)!=null
    }

    private fun getBubbleMessageViewBuilder(): MessageView.Builder{
        return MessageView.Builder()
            .from(mActivity.get()!!)
            .arrowPosition(mArrowPositionList)
            .backgroundColor(mBackgroundColor)
            .textColor(mTextColor)
            .titleTextSize(mTitleTextSize)
            .subtitleTextSize(mSubtitleTextSize)
            .title(mTitle)
            .subtitle(mSubtitle)
            .image(mImage)
            .closeActionImage(mCloseAction)
            .disableCloseAction(mDisableCloseAction)
            .listener(object : MessageViewListener {
                override fun onBubbleClick() {
                    mShowCaseListener?.onBubbleClick(this@ShowCase)
                }

                override fun onCloseActionImageClick() {
                    dismiss()
                    mShowCaseListener?.onCloseActionImageClick(this@ShowCase)
                }
            })
    }

    private fun registerBubbleShowCaseInPreferences(id: String){
        val mPrefs = mActivity.get()!!.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        setString(mPrefs, id, id)
    }

    private fun getString(mPrefs: SharedPreferences, key: String): String? {
        return mPrefs.getString(key, null)
    }

    private fun setString(mPrefs: SharedPreferences, key: String, value: String) {
        val editor = mPrefs.edit()
        editor.putString(key, value)
        editor.apply()
    }


    /**
     * This function takes a screenshot of the targetView, creating an ImageView from it. This new ImageView is also set on the layout passed by param
     */
    private fun addTargetViewAtBackgroundDimLayout(targetView: View?, backgroundDimLayout: RelativeLayout?) {
        if(targetView==null) return

        val targetScreenshot = takeScreenshot(targetView, mHighlightMode)
        val targetScreenshotView = ImageView(mActivity.get()!!)
        targetScreenshotView.setImageBitmap(targetScreenshot)
        targetScreenshotView.setOnClickListener {
            if(!mDisableTargetClick)
                dismiss()
            mShowCaseListener?.onTargetClick(this)
        }

        val targetViewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        targetViewParams.setMargins(getXposition(targetView), getYposition(targetView), getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width), 0)
        backgroundDimLayout?.addView(Animation.setBouncingAnimation(targetScreenshotView, 0, DURATION_BEATING_ANIMATION), targetViewParams)
    }

    /**
     * This function creates the BubbleMessageView depending the position of the target and the desired arrow position. This new view is also set on the layout passed by param
     */
    private fun addBubbleMessageViewDependingOnTargetView(targetView: View?, messageViewBuilder: MessageView.Builder, backgroundDimLayout: RelativeLayout?) {
        if(targetView==null) return
        val showCaseParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)

        when (messageViewBuilder.mArrowPosition[0]) {
            ArrowPosition.LEFT -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                if(Screen.isViewLocatedAtHalfTopOfTheScreen(mActivity.get()!!, targetView)){
                    showCaseParams.setMargins(
                        getXposition(targetView) + targetView.width,
                        getYposition(targetView),
                        if(isTablet()) getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width) - getMessageViewWidthOnTablet(getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width)) else 0,
                        0)
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                } else{
                    showCaseParams.setMargins(
                        getXposition(targetView) + targetView.width,
                        0,
                        if(isTablet()) getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width) - getMessageViewWidthOnTablet(getScreenWidth(mActivity.get()!!) - (getXposition(targetView) + targetView.width)) else 0,
                        getScreenHeight(mActivity.get()!!) - getYposition(targetView) - targetView.height)
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }
            ArrowPosition.RIGHT -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                if(Screen.isViewLocatedAtHalfTopOfTheScreen(mActivity.get()!!, targetView)){
                    showCaseParams.setMargins(
                        if(isTablet()) getXposition(targetView) - getMessageViewWidthOnTablet(getXposition(targetView)) else 0,
                        getYposition(targetView),
                        getScreenWidth(mActivity.get()!!) - getXposition(targetView),
                        0)
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                } else{
                    showCaseParams.setMargins(
                        if(isTablet()) getXposition(targetView) - getMessageViewWidthOnTablet(getXposition(targetView)) else 0,
                        0,
                        getScreenWidth(mActivity.get()!!) - getXposition(targetView),
                        getScreenHeight(mActivity.get()!!) - getYposition(targetView) - targetView.height)
                    showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }
            ArrowPosition.TOP -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                if(Screen.isViewLocatedAtHalfLeftOfTheScreen(mActivity.get()!!, targetView)){
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) else 0,
                        getYposition(targetView) + targetView.height,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - getMessageViewWidthOnTablet(getScreenWidth(mActivity.get()!!) - getXposition(targetView)) else 0,
                        0)
                } else{
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) + targetView.width - getMessageViewWidthOnTablet(getXposition(targetView)) else 0,
                        getYposition(targetView) + targetView.height,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - targetView.width else 0,
                        0)
                }
            }
            ArrowPosition.BOTTOM -> {
                showCaseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                if(Screen.isViewLocatedAtHalfLeftOfTheScreen(mActivity.get()!!, targetView)){
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) else 0,
                        0,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - getMessageViewWidthOnTablet(getScreenWidth(mActivity.get()!!) - getXposition(targetView)) else 0,
                        getScreenHeight(mActivity.get()!!) - getYposition(targetView))
                } else {
                    showCaseParams.setMargins(
                        if (isTablet()) getXposition(targetView) + targetView.width - getMessageViewWidthOnTablet(getXposition(targetView)) else 0,
                        0,
                        if (isTablet()) getScreenWidth(mActivity.get()!!) - getXposition(targetView) - targetView.width else 0,
                        getScreenHeight(mActivity.get()!!) - getYposition(targetView))
                }
            }
        }

        val bubbleMessageView = messageViewBuilder.targetViewScreenLocation(RectF(
            getXposition(targetView).toFloat(),
            getYposition(targetView).toFloat(),
            getXposition(targetView).toFloat() + targetView.width,
            getYposition(targetView).toFloat() + targetView.height))
            .build()

        bubbleMessageView.id = createViewId()
        val animation = Animation.getScaleAnimation(0, DURATION_SHOW_CASE_ANIMATION)
        backgroundDimLayout?.addView(Animation.setAnimationToView(bubbleMessageView, animation), showCaseParams)
    }

    /**
     * This function creates a BubbleMessageView and it is set on the center of the layout passed by param
     */
    private fun addBubbleMessageViewOnScreenCenter(messageViewBuilder: MessageView.Builder, backgroundDimLayout: RelativeLayout?) {
        val showCaseParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        showCaseParams.addRule(RelativeLayout. CENTER_VERTICAL)
        val messageView: MessageView = messageViewBuilder.build()
        messageView.id = createViewId()
        if(isTablet()) showCaseParams.setMargins(
            if (isTablet()) getScreenWidth(mActivity.get()!!)/2 - Screen.dpToPx(MAX_WIDTH_MESSAGE_VIEW_TABLET)/2 else 0,
            0,
            if (isTablet()) getScreenWidth(mActivity.get()!!)/2 - Screen.dpToPx(MAX_WIDTH_MESSAGE_VIEW_TABLET)/2 else 0,
            0)
        val animation = Animation.getScaleAnimation(0, DURATION_SHOW_CASE_ANIMATION)
        backgroundDimLayout?.addView(Animation.setAnimationToView(messageView, animation), showCaseParams)
    }

    private fun createViewId(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            View.generateViewId()
        } else {
            System.currentTimeMillis().toInt() / 1000
        }
    }

    private fun takeScreenshot(targetView: View, highlightMode: HighlightMode?): Bitmap? {
        if (highlightMode==null || highlightMode == HighlightMode.VIEW_LAYOUT)
            return takeScreenshotOfLayoutView(targetView)
        return takeScreenshotOfSurfaceView(targetView)
    }

    private fun takeScreenshotOfLayoutView(targetView: View): Bitmap? {
        if (targetView.width == 0 || targetView.height == 0) {
            return null
        }

        val rootView = getViewRoot(mActivity.get()!!)
        val currentScreenView = rootView.getChildAt(0)
        currentScreenView.buildDrawingCache()
        val bitmap: Bitmap
        bitmap = Bitmap.createBitmap(currentScreenView.drawingCache, getXposition(targetView), getYposition(targetView), targetView.width, targetView.height)
        currentScreenView.isDrawingCacheEnabled = false
        currentScreenView.destroyDrawingCache()
        return bitmap
    }

    private fun takeScreenshotOfSurfaceView(targetView: View): Bitmap? {
        if (targetView.width == 0 || targetView.height == 0) {
            return null
        }

        targetView.isDrawingCacheEnabled = true
        val bitmap: Bitmap = Bitmap.createBitmap(targetView.drawingCache)
        targetView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun isVisibleOnScreen(targetView: View?): Boolean {
        if(targetView!=null){
            if(getXposition(targetView) >= 0 && getYposition(targetView) >= 0){
                return getXposition(targetView) != 0 || getYposition(targetView) != 0
            }
        }
        return false
    }

    private fun getXposition(targetView: View): Int{
        return Screen.getAxisXpositionOfViewOnScreen(targetView) - getScreenHorizontalOffset()
    }

    private fun getYposition(targetView: View): Int{
        return Screen.getAxisYpositionOfViewOnScreen(targetView) - getScreenVerticalOffset()
    }

    private fun getScreenHeight(context: Context): Int{
        return Screen.getScreenHeight(context) - getScreenVerticalOffset()
    }

    private fun getScreenWidth(context: Context): Int{
        return Screen.getScreenWidth(context) - getScreenHorizontalOffset()
    }

    private fun getScreenVerticalOffset(): Int{
        return if(backgroundDimLayout !=null) Screen.getAxisYpositionOfViewOnScreen(backgroundDimLayout!!) else 0
    }

    private fun getScreenHorizontalOffset(): Int{
        return if(backgroundDimLayout !=null) Screen.getAxisXpositionOfViewOnScreen(backgroundDimLayout!!) else 0
    }

    private fun getMessageViewWidthOnTablet(availableSpace: Int): Int{
        return if(availableSpace > Screen.dpToPx(MAX_WIDTH_MESSAGE_VIEW_TABLET)) Screen.dpToPx(MAX_WIDTH_MESSAGE_VIEW_TABLET) else availableSpace
    }

    private fun isTablet(): Boolean = mActivity.get()!!.resources.getBoolean(R.bool.isTablet)


}