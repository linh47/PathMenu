package com.ai.ipu.ipu_pathmenu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class PathMenuService extends Service {
	PathMenu pathMenu;
	private static final int[] ITEM_DRAWABLES = { R.drawable.composer_close,
		R.drawable.composer_music, R.drawable.composer_place,
		R.drawable.composer_sleep, R.drawable.composer_thought,
		R.drawable.composer_with };

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		pathMenu = new PathMenu(getApplicationContext());
		initPathMenu(pathMenu, ITEM_DRAWABLES);// 初始化子菜单
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (pathMenu != null) {
			pathMenu.hidePathMenu();
		}
	}
	
	/**
	 * 初始化子菜单图片、点击事件
	 * 
	 * @param menu
	 * @param itemDrawables
	 */
	private void initPathMenu(final PathMenu menu, int[] itemDrawables) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(this);
			item.setImageResource(itemDrawables[i]);
			final int index = i;
			menu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (index) {
					case 0:
						Toast.makeText(PathMenuService.this, "第0个被点击，关闭菜单",
								Toast.LENGTH_SHORT).show();
						stopSelf();//关闭菜单
						break;

					case 1:
						Toast.makeText(PathMenuService.this, "第1个被点击",
								Toast.LENGTH_SHORT).show();
						break;
						
					}
				}
			});
		}
	}
	
}
