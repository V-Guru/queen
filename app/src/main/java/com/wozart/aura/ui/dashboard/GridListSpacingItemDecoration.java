package com.wozart.aura.ui.dashboard;

import android.graphics.Rect;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class GridListSpacingItemDecoration extends RecyclerView.ItemDecoration {

  private final int spacing;

  public GridListSpacingItemDecoration(int spacingPixels) {
    spacing = spacingPixels;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();

    int spanCount = layoutManager.getSpanCount();
    int position = parent.getChildAdapterPosition(view);
    int column = position % spanCount;

    outRect.left = column * spacing / spanCount;
    outRect.right = spacing - (column + 1) * spacing / spanCount;
    if (position >= spanCount) {
      outRect.top = spacing;
    }
  }
}