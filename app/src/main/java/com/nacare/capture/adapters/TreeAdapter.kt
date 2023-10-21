package com.nacare.capture.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.nacare.capture.models.OrgTreeNode


class TreeAdapter(
    private val context: Context,
    private val treeNodes: List<OrgTreeNode>,
    private val click: (OrgTreeNode) -> Unit
) :
    RecyclerView.Adapter<TreeAdapter.TreeViewHolder>() {

    inner class TreeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.tree_item_layout, parent, false)
        return TreeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return treeNodes.size
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        val node = treeNodes[position]
        val itemView = holder.itemView

        // Adjust the indentation based on the tree level
        val paddingLeft = 20 * getTreeLevel(node)
        itemView.setPadding(paddingLeft, 0, 0, 0)

        // Set label text
        val labelTextView: TextView = itemView.findViewById(R.id.org_unit_name)
        labelTextView.text = node.label.trim()

        // Handle click to expand/collapse
        itemView.setOnClickListener {
            toggleNode(position, holder)
        }

        // Handle visibility based on whether the node has children and is expanded
        val arrowIcon: ImageView = itemView.findViewById(R.id.org_unit_icon)
        arrowIcon.visibility = if (node.children.isNotEmpty()) View.VISIBLE else View.INVISIBLE

        // Set label text
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        checkbox.visibility = if (node.children.isNotEmpty()) View.INVISIBLE else View.VISIBLE
        // Handle visibility based on whether the node has children and is expanded
        if (node.children.isEmpty()) {
            checkbox.setOnClickListener {
                val patient = treeNodes[position]
                click(patient)
            }
        }
        // Handle expand/collapse state
//        arrowIcon.rotation = if (node.isExpanded) 90f else 0f
        arrowIcon.setImageResource(if (node.isExpanded) R.drawable.baseline_remove_circle_24 else R.drawable.ic_add_circle)

        // Set up child RecyclerView
        val childRecyclerView: RecyclerView = itemView.findViewById(R.id.childRecyclerView)
        val childAdapter = TreeAdapter(context, node.children, click)
        childRecyclerView.adapter = childAdapter
        childRecyclerView.layoutManager = LinearLayoutManager(context)

        // Set child RecyclerView visibility based on the expand/collapse state
        childRecyclerView.visibility = if (node.isExpanded) View.VISIBLE else View.GONE
    }

    private fun getTreeLevel(node: OrgTreeNode): Int {
        var level = 0
        var currentNode = node
        while (currentNode.children.isNotEmpty()) {
            level++
            currentNode = currentNode.children[0]
        }
        return level
    }

    private fun toggleNode(position: Int, holder: TreeViewHolder) {
        val node = treeNodes[position]
        node.isExpanded = !node.isExpanded
        notifyItemChanged(position)

        // Update child RecyclerView visibility
        val childRecyclerView: RecyclerView = holder.itemView.findViewById(R.id.childRecyclerView)
        childRecyclerView.visibility = if (node.isExpanded) View.VISIBLE else View.GONE
    }
}
