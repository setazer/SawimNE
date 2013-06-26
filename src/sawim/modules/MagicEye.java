/*******************************************************************************
Jimm - Mobile Messaging - J2ME ICQ clone
Copyright (C) 2003-05  Jimm Project

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
********************************************************************************
File: src/jimm/DebugLog.java
Version: ###VERSION###  Date: ###DATE###
Author(s): Artyomov Denis
*******************************************************************************/

package sawim.modules;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import protocol.*;
import DrawControls.icons.Icon;
import ru.sawim.models.form.VirtualListItem;
import sawim.Sawim;
import sawim.comm.Util;
import sawim.ui.base.Scheme;
import sawim.ui.text.VirtualList;
import sawim.ui.text.VirtualListModel;
import sawim.util.JLocale;

public final class MagicEye {
    public static final MagicEye instance = new MagicEye();

    private VirtualList list;
    private VirtualListModel model = new VirtualListModel();

    private static final int MENU_COPY      = 0;
    private static final int MENU_COPY_ALL  = 1;
    private static final int MENU_CLEAN     = 2;
    private static final int MENU_USER_MENU = 3;

    public void activate() {
        list = VirtualList.getInstance();
        list.setCaption(JLocale.getString("magic eye"));
        list.setBuildOptionsMenu(new VirtualList.OnBuildOptionsMenu() {
            @Override
            public void onCreateOptionsMenu(Menu menu) {
                menu.add(Menu.FIRST, MENU_CLEAN, 2, JLocale.getString("clear"));
            }

            @Override
            public void onOptionsItemSelected(FragmentActivity activity, MenuItem item) {
                switch (item.getItemId()) {
                    case MENU_CLEAN:
                        synchronized (instance) {
                            model.clear();
                        }
                        break;
                }
            }
        });
        list.setModel(model);
        instance.list.show();
    }

    private synchronized void registerAction(Protocol protocol, String userId,
            String action, String msg, Icon icon) {

        String date = Util.getLocalDateString(Sawim.getCurrentGmtTime(), true);
        action = JLocale.getString(action);
        Contact contact = protocol.getItemByUIN(userId);

        VirtualListItem record = model.createNewParser(true);
		if (icon != null) {
		    record.addImage(icon.getImage());
		}
        String label = date + ": ";
        if (null == contact) {
            label += userId;
        } else {
            label += contact.getName();
        }
        record.addLabel(label, Scheme.THEME_MAGIC_EYE_USER, Scheme.FONT_STYLE_PLAIN);
        String decs = action + " ";
        if (null != msg) {
            decs += msg;
        }
        record.addDescriptionSelectable(decs, Scheme.THEME_MAGIC_EYE_TEXT, Scheme.FONT_STYLE_PLAIN);

        model.addPar(record);
        removeOldRecords();
    }

    private void removeOldRecords() {
        final int maxRecordCount = 50;
        while (maxRecordCount < model.getSize()) {
            list.removeFirstText();
        }
    }

    public static void addAction(Protocol protocol, String userId, String action, String msg, Icon icon) {
        instance.registerAction(protocol, userId, action, msg, icon);
    }
    public static void addAction(Protocol protocol, String userId, String action, String msg) {
        instance.registerAction(protocol, userId, action, msg, null);
    }

    public static void addAction(Protocol protocol, String userId, String action) {
        instance.registerAction(protocol, userId, action, null, null);
    }
}