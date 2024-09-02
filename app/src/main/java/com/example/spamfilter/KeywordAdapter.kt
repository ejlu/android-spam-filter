package com.example.spamfilter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spamfilter.databinding.ItemKeywordBinding

class KeywordAdapter(
    private val keywords: List<String>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val binding = ItemKeywordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.bind(keywords[position])
    }

    override fun getItemCount() = keywords.size

    inner class KeywordViewHolder(private val binding: ItemKeywordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteButton.setOnClickListener {
                // Use bindingAdapterPosition instead of adapterPosition
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(position)
                }
            }
        }

        fun bind(keyword: String) {
            binding.keywordTextView.text = keyword
        }
    }
}