package io.instaal.bubbleshow

/**
 * Listener of user actions in a BubbleShowCase
 */
interface ShowCaseListener {
    /**
     * It is called when the user clicks on the targetView
     */
    fun onTargetClick(showCase: ShowCase)

    /**
     * It is called when the user clicks on the close icon
     */
    fun onCloseActionImageClick(showCase: ShowCase)

    /**
     * It is called when the user clicks on the background dim
     */
    fun onBackgroundDimClick(showCase: ShowCase)

    /**
     * It is called when the user clicks on the bubble
     */
    fun onBubbleClick(showCase: ShowCase)
}