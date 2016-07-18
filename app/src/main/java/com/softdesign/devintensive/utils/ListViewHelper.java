package com.softdesign.devintensive.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewHelper {

    /**
     * Устанавливает высоту ListView динамически исходя из высоты, содержащихся в нем элементов.     *
     * @param listView виджет ListView, для которого определяется высота
     * @return true если высота listView успешно изменена, false - в противном случае
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Получаем общую высоту всех элементов списка.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Получаем общую высоту всех разделителй элементов списка.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Задает высоту всех элементов списка.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }
}
