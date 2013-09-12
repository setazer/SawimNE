package ru.sawim.models;

import DrawControls.icons.Icon;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;

import android.support.v4.app.FragmentActivity;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import protocol.Protocol;
import ru.sawim.General;
import ru.sawim.R;
import ru.sawim.text.InternalURLSpan;
import ru.sawim.view.MyTextView;
import ru.sawim.view.menu.JuickMenu;
import sawim.Clipboard;
import ru.sawim.text.TextFormatter;
import sawim.chat.Chat;
import sawim.chat.MessData;
import sawim.chat.message.Message;
import ru.sawim.Scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gerc
 * Date: 04.04.13
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */
public class MessagesAdapter extends BaseAdapter {

    private static FragmentActivity activity;
    private List<MessData> items = new ArrayList<MessData>();
    private static Protocol currentProtocol;
    private static String currentContact;
    private boolean isSingleUserContact;
    private static TextFormatter textFormatter = new TextFormatter();

    private boolean isMultiСitation = false;

    public void init(FragmentActivity activity, Chat chat) {
        this.activity = activity;
        currentProtocol = chat.getProtocol();
        currentContact = chat.getContact().getUserId();
        isSingleUserContact = chat.getContact().isSingleUserContact();
        refreshList(chat.getMessData());
    }

    public void refreshList(List<MessData> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    public boolean isMultiСitation() {
        return isMultiСitation;
    }

    public void setMultiСitation(boolean multiShot) {
        isMultiСitation = multiShot;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public MessData getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int index, View row, ViewGroup viewGroup) {
        if (row == null) {
            row = new MessageItemView(activity);
            ((MessageItemView) row).build();
        }
        final MessageItemView item = ((MessageItemView) row);
        final MessData mData = items.get(index);
        String nick = mData.getNick();
        boolean incoming = mData.isIncoming();

        ((ViewGroup)row).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        item.msgText.setOnTextLinkClickListener(textLinkClickListener);
        byte bg;
        if (mData.isMarked()) {
            bg = Scheme.THEME_CHAT_BG_MARKED;
            item.msgText.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (mData.isService())
            bg = Scheme.THEME_CHAT_BG_SYSTEM;
        else if ((index & 1) == 0)
            bg = incoming ? Scheme.THEME_CHAT_BG_IN : Scheme.THEME_CHAT_BG_OUT;
        else
            bg = incoming ? Scheme.THEME_CHAT_BG_IN_ODD : Scheme.THEME_CHAT_BG_OUT_ODD;
        row.setBackgroundColor(Scheme.getColor(bg));
        if (mData.isMe() || mData.isPresence()) {
            item.msgImage.setVisibility(ImageView.GONE);
            item.msgNick.setVisibility(TextView.GONE);
            item.msgTime.setVisibility(TextView.GONE);
            item.msgText.setTextHash(mData.parsedText().toString());
            item.msgText.setTextSize(General.getFontSize() - 2);
            if (mData.isMe()) {
                item.msgText.setTextColor(Scheme.getColor(incoming ? Scheme.THEME_CHAT_INMSG : Scheme.THEME_CHAT_OUTMSG));
                item.msgText.setText("* " + nick + " " + mData.parsedText());
            } else {
                item.msgText.setTextColor(Scheme.getColor(Scheme.THEME_CHAT_INMSG));
                item.msgText.setText(nick + mData.parsedText());
            }
        } else {
            if (mData.iconIndex != Message.ICON_NONE) {
                Icon icon = Message.msgIcons.iconAt(mData.iconIndex);
                if (icon == null) {
                    item.msgImage.setVisibility(ImageView.GONE);
                } else {
                    item.msgImage.setVisibility(ImageView.VISIBLE);
                    item.msgImage.setImageDrawable(icon.getImage());
                }
            }

            item.msgNick.setVisibility(TextView.VISIBLE);
            item.msgNick.setTextColor(Scheme.getColor(incoming ? Scheme.THEME_CHAT_INMSG : Scheme.THEME_CHAT_OUTMSG));
            item.msgNick.setTypeface(Typeface.DEFAULT_BOLD);
            item.msgNick.setTextSize(General.getFontSize());
            item.msgNick.setTextHash(nick);
            item.msgNick.setText(nick);

            item.msgTime.setVisibility(TextView.VISIBLE);
            item.msgTime.setTextColor(Scheme.getColor(incoming ? Scheme.THEME_CHAT_INMSG : Scheme.THEME_CHAT_OUTMSG));
            item.msgTime.setTextSize(General.getFontSize() - 4);
            item.msgTime.setText(mData.strTime);

            byte color = Scheme.THEME_TEXT;
            if (incoming && !isSingleUserContact && mData.isHighLight())
                color = Scheme.THEME_CHAT_HIGHLIGHT_MSG;

            item.msgText.setTextColor(Scheme.getColor(color));
            item.msgText.setTextSize(General.getFontSize());
            item.msgText.setTextHash(mData.parsedText().toString());
            item.msgText.setText(mData.parsedText());
        }
        return row;
    }
    public static InternalURLSpan.TextLinkClickListener textLinkClickListener = new InternalURLSpan.TextLinkClickListener() {
        @Override
        public void onTextLinkClick(View textView, final String clickedString) {
            if (clickedString.length() == 0) return;
            if (clickedString.substring(0, 1).equals("@") || clickedString.substring(0, 1).equals("#")) {
                new JuickMenu(activity, currentProtocol, currentContact, clickedString).show();
            } else {
                CharSequence[] items = new CharSequence[2];
                items[0] = activity.getString(R.string.copy);
                items[1] = activity.getString(R.string.add_contact);
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setCancelable(true);
                builder.setTitle(R.string.url_menu);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Clipboard.setClipBoardText(clickedString);
                                break;
                            case 1:
                                General.openUrl(clickedString);
                                break;
                        }
                    }
                });
                try {
                    builder.create().show();
                } catch(Exception e){
                    // WindowManager$BadTokenException will be caught and the app would not display
                }
            }
        }
    };
}