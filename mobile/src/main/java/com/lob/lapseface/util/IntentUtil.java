package com.lob.lapseface.util;

import android.content.Intent;
import android.net.Uri;

public final class IntentUtil {
    public static Intent home() {
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static Intent mail(String mailAddress) {
        return new Intent(Intent.ACTION_SEND)
                .setType("plain/text")
                .putExtra(Intent.EXTRA_EMAIL, new String[]{mailAddress});
    }

    public static Intent browser(String website) {
        return new Intent(Intent.ACTION_VIEW).setData(Uri.parse(website));
    }
}
