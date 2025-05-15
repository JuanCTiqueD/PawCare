package com.app.pawcare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            val view = layoutInflater.inflate(R.layout.item_chat_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else { // VIEW_TYPE_MESSAGE_RECEIVED
            val view = layoutInflater.inflate(R.layout.item_chat_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun clearMessages() {
        val size = messages.size
        messages.clear()
        if (size > 0) {
            notifyItemRangeRemoved(0, size)
        }
    }

    abstract class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: ChatMessage)

        protected fun formatTimestamp(timestamp: Long): String {
            if (timestamp == 0L) return ""
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    inner class SentMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessageTextSent)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.tvMessageTimestampSent)
        private val messageImage: ImageView = itemView.findViewById(R.id.ivMessageImageSent)

        override fun bind(message: ChatMessage) {
            if (!message.text.isNullOrEmpty()) {
                messageText.text = message.text
                messageText.visibility = View.VISIBLE
            } else {
                messageText.visibility = View.GONE
            }

            if (!message.imageUrl.isNullOrEmpty()) {
                messageImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(messageImage)
            } else {
                messageImage.visibility = View.GONE
            }
            messageTimestamp.text = formatTimestamp(message.timestamp)
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessageTextReceived)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.tvMessageTimestampReceived)
        private val messageImage: ImageView = itemView.findViewById(R.id.ivMessageImageReceived)
        private val senderNameTextView: TextView? = itemView.findViewById(R.id.tvSenderNameReceived)

        override fun bind(message: ChatMessage) {
            if (senderNameTextView != null && !message.senderName.isNullOrEmpty()) {
                senderNameTextView.text = message.senderName
                senderNameTextView.visibility = View.VISIBLE
            } else {
                senderNameTextView?.visibility = View.GONE
            }

            if (!message.text.isNullOrEmpty()) {
                messageText.text = message.text
                messageText.visibility = View.VISIBLE
            } else {
                messageText.visibility = View.GONE
            }

            if (!message.imageUrl.isNullOrEmpty()) {
                messageImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .into(messageImage)
            } else {
                messageImage.visibility = View.GONE
            }
            messageTimestamp.text = formatTimestamp(message.timestamp)
        }
    }
}