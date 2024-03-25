package com.terrranullius.lockout.ui.util

import android.content.Context
import android.view.Menu
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.terrranullius.lockout.R

data class PopupItem(
    val title: String,
    val onClick: () -> Unit
)

fun View.showPopup(
    items: List<PopupItem>
) {
    val wrapper: Context = ContextThemeWrapper(this.context, R.style.Theme_LockOut)
    val popMenu = androidx.appcompat.widget.PopupMenu(
        wrapper,
        this
    )
    val menu = popMenu.menu

    //Item id of a menu item is its index in the list
    items.forEachIndexed { index, popupItem ->
        menu.add(
            Menu.NONE,
            index,
            Menu.NONE,
            popupItem.title
        )
    }
    popMenu.setOnMenuItemClickListener {
        val clickedItem = items.getOrNull(it.itemId)
        clickedItem?.onClick?.let { it1 -> it1() }
        return@setOnMenuItemClickListener true
    }

    popMenu.show()
}
