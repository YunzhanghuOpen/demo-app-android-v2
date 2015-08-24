package io.rong.app.activity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.sea_monster.resource.Resource;

import java.util.List;

import io.rong.app.R;
import io.rong.imkit.RLog;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by zhjchen on 8/12/15.
 */
public abstract class LocationMapActivity extends BasicMapActivity implements AMap.OnMarkerClickListener {


    public void addMarker(LatLng latLng, final UserInfo userInfo) {

        final String url = userInfo.getPortraitUri().toString();
        Log.d("LocationMapActivity", "LocationMapActivity addMarker-- url:" + url);

        final MarkerOptions markerOption = new MarkerOptions();

        markerOption.position(latLng);

        View view = LayoutInflater.from(this).inflate(R.layout.rc_icon_rt_location_marker, null);
        AsyncImageView imageView = (AsyncImageView) view.findViewById(android.R.id.icon);
        ImageView locImageView = (ImageView) view.findViewById(android.R.id.icon1);

        if (userInfo.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
            locImageView.setImageResource(R.drawable.rc_rt_loc_myself);
        } else {
            locImageView.setImageResource(R.drawable.rc_rt_loc_other);
        }

        imageView.setResource(new Resource(url));

        markerOption.anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromView(view));

        Marker marker = getaMap().addMarker(markerOption);
        marker.setObject(userInfo.getUserId());

    }

    public void removeMarker(String userId) {

        RLog.e(this, "removeMarker", "removeMarker:userId---" + userId);

        List<Marker> markers = getaMap().getMapScreenMarkers();

        for (Marker marker : markers) {
            RLog.e(this, "removeMarker", "removeMarker:getObject---" + marker.getObject());
            if (marker.getObject() != null && userId.equals(marker.getObject())) {
                marker.remove();
                break;
            }
        }
        RLog.e(this, "removeMarker", "------------------end");
    }


    public void moveMarker(LatLng latLng, UserInfo userInfo) {
        removeMarker(userInfo.getUserId());
        addMarker(latLng, userInfo);
    }

    /**
     * 对marker标注点点击响应事件
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        return true;
    }


}
