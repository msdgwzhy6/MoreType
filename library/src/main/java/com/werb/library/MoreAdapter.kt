package com.werb.library

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import com.werb.library.extension.AlphaAnimation
import com.werb.library.extension.AnimExtension
import com.werb.library.extension.MoreAnimation
import com.werb.library.link.MoreLink
import com.werb.library.link.MoreLinkManager
import com.werb.library.link.MultiLink
import kotlin.reflect.KClass
import android.view.animation.LinearInterpolator
import com.werb.library.action.DataAction


/**
 * [MoreAdapter] build viewHolder with data
 * Created by wanbo on 2017/7/2.
 */
class MoreAdapter : Adapter<ViewHolder>(), MoreLink, AnimExtension, DataAction {

    private var list: MutableList<Any> = mutableListOf()
    private val linkManager = MoreLinkManager(this)
    private var animation: MoreAnimation? = null
    private var animDuration = 250L
    private var startAnimPosition = 0
    private var firstShow = false
    private var lastAnimPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val moreViewType = buildViewType(viewType)
        if (moreViewType == null){
            return attachViewType(list[viewType]).onCreateViewHolder(LayoutInflater.from(parent?.context), parent as ViewGroup)
        }else {
            return moreViewType.onCreateViewHolder(LayoutInflater.from(parent?.context), parent as ViewGroup)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val any = list[position]
        val attachViewType = attachViewType(any)
        attachViewType.bindHolder(holder as MoreViewHolder)
        attachViewType.bindData(any, holder)
    }

    fun attachTo(view: RecyclerView): MoreLink {
        view.adapter = this
        return this
    }


    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any) {
        if (data is List<*>) {
            var position = 0
            if (list.size > 0){
                position = list.size
            }
            list.addAll(position, data as Collection<Any>)
            notifyItemRangeInserted(position, data.size)
        } else {
            list.add(data)
            notifyItemInserted(list.indexOf(data))
        }
    }

    override fun getData(position: Int): Any = list[position]

    override fun removeAllData() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun removeData(data: Any) {
        val contains = list.contains(data)
        if (contains) {
            val index = list.indexOf(data)
            removeData(index)
        }
    }

    override fun removeData(position: Int) {
        if(list.size == 0){
            return
        }
        if (position >= 0 && position <= list.size - 1){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        val any = list[position]
        val viewLayout = attachViewTypeLayout(any)
        if (viewLayout == -1){
            return position
        }else {
            return viewLayout
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder?) {
        super.onViewAttachedToWindow(holder)
        addAnimation(holder as MoreViewHolder)
    }

    /** [renderWithAnimation] user default animation AlphaAnimation */
    override fun renderWithAnimation() : MoreAdapter{
        this.animation = AlphaAnimation()
        return this
    }

    /** [renderWithAnimation] user custom animation */
    override fun renderWithAnimation(animation: MoreAnimation) : MoreAdapter {
        this.animation = animation
        return this
    }

    /** [addAnimation] addAnimation when view attached to windows */
    override fun addAnimation(holder: MoreViewHolder) {
        this.animation?.let {
            if(holder.layoutPosition < startAnimPosition){
                return
            }
            if(!firstShow || holder.layoutPosition > lastAnimPosition) {
                val animators = it.getItemAnimators(holder.itemView)
                for (anim in animators) {
                    anim.setDuration(animDuration).start()
                    anim.interpolator = LinearInterpolator()
                }
                lastAnimPosition = holder.layoutPosition
            }
        }
    }

    /** [duration] set animation duration */
    override fun duration(duration: Long) : MoreAdapter {
        this.animDuration = duration
        return this
    }

    /** [firstShowAnim] set isShow animation when first display  */
    override fun firstShowAnim(firstShow: Boolean): MoreAdapter {
        this.firstShow = firstShow
        return  this
    }

    /** [startAnimPosition] set animation start position */
    override fun startAnimPosition(position: Int): MoreAdapter {
        this.startAnimPosition = position
        return this
    }

    /** [register] register viewType which single link with model  */
    override fun register(viewType: MoreViewType<*>): MoreAdapter {
        linkManager.register(viewType)
        return this
    }

    /** [multiRegister] multiRegister viewType like one2more case , user MultiLink to choose which one is we need */
    override fun multiRegister(clazz: KClass<out Any>, link: MultiLink<*>): MoreAdapter {
        linkManager.multiRegister(clazz, link)
        return this
    }

    /** [attachViewType]  find viewType by item of list */
    override fun attachViewType(any: Any): MoreViewType<Any> = linkManager.attachViewType(any)

    /** [attachViewTypeLayout]  find viewType layout by item of list */
    override fun attachViewTypeLayout(any: Any): Int = linkManager.attachViewTypeLayout(any)

    /** [buildViewType]  find viewType by layout */
    override fun buildViewType(type: Int): MoreViewType<Any>? = linkManager.buildViewType(type)

    /** [userSoleRegister] register sole global viewType */
    override fun userSoleRegister(): MoreAdapter = linkManager.userSoleRegister()
}