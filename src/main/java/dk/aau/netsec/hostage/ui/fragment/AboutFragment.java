package dk.aau.netsec.hostage.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

import dk.aau.netsec.hostage.Hostage;
import dk.aau.netsec.hostage.R;

/**
 * Shows informations about the developers of the app
 * <p>
 * Created by Fabio Arnold on 25.02.14.
 * displays credits for the app
 */
public class AboutFragment extends Fragment {
    private View rootView;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;

    @SuppressLint("SetTextI18n")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.inflater = inflater;
        this.container = container;
        this.savedInstanceState = savedInstanceState;
        final Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getResources().getString(R.string.drawer_app_info));
        }

        rootView = inflater.inflate(R.layout.fragment_about, container, false);
        PackageManager manager = Hostage.getContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(Hostage.getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionApp;
        assert info != null;
        versionApp = info.versionName;

        TextView hostage = rootView.findViewById(R.id.hostage);
        TextView version = rootView.findViewById(R.id.hostageVersion);

        version.setText("ver. " + versionApp);
        hostage.setMovementMethod(LinkMovementMethod.getInstance());
        version.setMovementMethod(LinkMovementMethod.getInstance());

        return rootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (rootView != null) {
            unbindDrawables(rootView);
            rootView = null;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onCreateView(inflater, container, savedInstanceState);
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//        try {
//
//                getView().setFocusableInTouchMode(true);
//                getView().requestFocus();
//                getView().setOnKeyListener(new View.OnKeyListener() {
//                    @Override
//                    public boolean onKey(View v, int keyCode, KeyEvent event) {
//                        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
//                            // handle back button's click listener
//                            getFragmentManager().popBackStackImmediate();
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//
//        } catch (Exception e) {
//            System.out.print(e.toString());
//        }


}

    @Override
    public void onPause() {
        super.onPause();
        if (rootView != null) {
            unbindDrawables(rootView);
            rootView = null;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (rootView != null) {
            unbindDrawables(rootView);
            rootView = null;
        }
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
