package com.lob.lapseface;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;
import com.lob.lapseface.shared.exception.NotSameArrayLengthException;
import com.lob.lapseface.shared.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class SyncLaterService extends WearableListenerService {

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        ArrayList<Integer> indexesArrayList = SharedPreferencesUtil.StoreSyncLaterValues.getIndexes();
        ArrayList<Integer> valuesArrayList = SharedPreferencesUtil.StoreSyncLaterValues.getValues();
        if (indexesArrayList.size() != 0 && valuesArrayList.size() != 0) {
            if (indexesArrayList.size() == valuesArrayList.size()) {
                for (int i = 0; i < indexesArrayList.size(); i++) {
                    SharedPreferencesUtil.setSecondsForTimeLapse(
                            getApplicationContext(),
                            indexesArrayList.get(i),
                            valuesArrayList.get(i));
                }
                SharedPreferencesUtil.StoreSyncLaterValues.clearAll();
            } else {
                try {
                    throw new NotSameArrayLengthException();
                } catch (NotSameArrayLengthException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onConnectedNodes(connectedNodes);
    }
}
