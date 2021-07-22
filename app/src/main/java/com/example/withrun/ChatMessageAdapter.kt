package com.example.withrun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_chat_mesage_center.view.*
import kotlinx.android.synthetic.main.item_chat_mesage_left.view.*
import kotlinx.android.synthetic.main.item_chat_mesage_right.view.*
import java.text.SimpleDateFormat

class ChatMessageAdapter (val context: Context, val msgList: MutableList<Message>): RecyclerView.Adapter<ChatMessageAdapter.BaseViewHolder<*>>() {

    val TYPE_1 = 0 // 내가보낸 메시지
    val TYPE_2 = 1 // 남이보낸 메시지
    val TYPE_3 = 2 // 알림 메시지

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    inner class ViewHolder1(itemView:View):BaseViewHolder<Message>(itemView){
        val messageText = itemView.tvMessage
        val sendTime = itemView.tvTime
//        val ckMsg = itemView.checkmsg

        override fun bind(item: Message) { // 내가보낸 메시지
            messageText?.text = item.getMessageText()
            sendTime?.text = item.getMessageDate()?.let { Dateformat(it) }
//            ckMsg?.text = item.getMessageDate().toString()
            sendTime.setOnClickListener {
                Toast.makeText(context,"1번 타입", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ViewHolder2(itemView: View):BaseViewHolder<Message>(itemView){ // 남이보낸 메시지
        val ivProfileimg = itemView.ivProfileimg
        val tvNickName = itemView.tvNickName
        val tvMessage2 = itemView.tvMessage2
        val tvTime2 = itemView.tvTime2
        override fun bind(item: Message) {

            if (item.getProfileImgUrl() != "else") {
                if (ivProfileimg != null) {
                    Glide.with(context)
                        .load(Constants.URL + item.getProfileImgUrl())
                        .into(ivProfileimg)
                }
            } else {
                ivProfileimg?.setImageResource(R.drawable.avatar)
            }

            tvNickName?.text = item.getNickname()
            tvMessage2?.text = item.getMessageText()
            tvTime2?.text = item.getMessageDate()?.let { Dateformat(it) }
            tvTime2.setOnClickListener {
                Toast.makeText(context,"2번 타입", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ViewHolder3(itemView: View):BaseViewHolder<Message>(itemView){ // 중앙 알림메시지
        val centerMsg = itemView.tvCenterMsg
        override fun bind(item: Message) {
            centerMsg?.text = item.getMessageText()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType){
            TYPE_1 -> { // 내가 보낸 메시지
                val view = LayoutInflater.from(context).inflate(R.layout.item_chat_mesage_right, parent, false)
                ViewHolder1(view)
            }
            TYPE_2 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_chat_mesage_left, parent, false)
                ViewHolder2(view)
            }
            TYPE_3 -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_chat_mesage_center, parent, false)
                ViewHolder3(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = msgList[position]
        when(holder){
            is ViewHolder1 -> holder.bind(element as Message)
            is ViewHolder2 -> holder.bind(element as Message)
            is ViewHolder3 -> holder.bind(element as Message)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return msgList[position].getMessagetype()
    }

    override fun getItemCount(): Int {
        return msgList.size
    }
    fun Dateformat(sendTime: Long): String {
        val f = SimpleDateFormat("a hh:mm")
        return f.format(sendTime)
    }

    fun Dateformat2(sendTime: Long): String? {
        val f = SimpleDateFormat("ㅡ yyyy년 MM월 dd일 E요일 ㅡ")
        return f.format(sendTime)
    }
}