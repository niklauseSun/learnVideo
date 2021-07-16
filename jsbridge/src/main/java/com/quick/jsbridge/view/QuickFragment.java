package com.quick.jsbridge.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.donkingliang.imageselector.utils.ImageSelector;
import com.quick.core.baseapp.baseactivity.FrmBaseFragment;
import com.quick.core.baseapp.baseactivity.control.PageControl;
import com.quick.core.ui.app.IPageControl;
import com.quick.core.util.common.JsonUtil;
import com.quick.jsbridge.bean.QuickBean;
import com.quick.jsbridge.control.AutoCallbackDefined;
import com.quick.jsbridge.control.WebloaderControl;
import com.quick.jsbridge.view.webview.QuickWebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import quick.com.jsbridge.R;



/**
 * Created by dailichun on 2017/12/7.
 * quick的fragment容器，如果要加载H5页面请使用{@link QuickWebLoader}
 */
public class QuickFragment extends FrmBaseFragment implements IQuickFragment {

    /**
     * tab的序号
     */
    public static int indexBottom;

    /**
     * 浏览器控件
     */
    private QuickWebView wv;



    /**
     * 初始化属性
     */
    private QuickBean bean;

    /**
     * 控制器
     */
    private WebloaderControl control;

    /**
     * H5加载进度条
     */
    private ProgressBar pb;

    public QuickFragment() {
    }

    public static QuickFragment newInstance(QuickBean bean) {
        QuickFragment fragment = new QuickFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", bean);
        bundle.putInt(PageControl.PAGE_STYLE, bean.pageStyle);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTheme(R.style.ActionSheetStyleIOS7);
        setLayout(R.layout.quick_fragment);

        bean = (QuickBean) getArguments().getSerializable("bean");

        //初始化控件
        initView();
    }

    /**
     * 初始化布局控件
     */
    protected void initView() {
        pb = (ProgressBar) findViewById(R.id.pb);
        wv = (QuickWebView) findViewById(R.id.wv);

        //初始化控制器
        control = new WebloaderControl(this, bean, wv);


        //设置错误状态页点击事件
        pageControl.getStatusPage().setClickButton(getString(R.string.status_page_reload),new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //加载历史记录最近一页
                control.loadLastPage(true);
            }
        });

        //加载页面
        control.loadPage();
    }

    @Override
    public IPageControl getPageControl() {
        return pageControl;
    }

    @Override
    public WebloaderControl getWebloaderControl() {
        return control;
    }

    @Override
    public QuickWebView getQuickWebView() {
        return wv;
    }

    @Override
    public void setQuickBean(QuickBean bean) {
        this.bean = bean;
    }

    @Override
    public QuickBean getQuickBean() {
        return bean;
    }

    @Override
    public ProgressBar getProgressBar() {
        return pb;
    }

    @Override
    public QuickFragment getQuickFragment() {
        return this;
    }

    public void startCamera(JSONObject param) {
        Boolean crop = "1".equals(param.optString("corp", "0"));
        ImageSelector.builder()
                .setCrop(crop) // 设置是否使用图片剪切功能。
                .setCropRatio(1.0f) // 图片剪切的宽高比,默认1.0f。宽固定为手机屏幕的宽。
                .onlyTakePhoto(true)  // 仅拍照，不打开相册
                .start(this, ImageSelector.RESULT_CODE);
    }

    public void selectImage(JSONObject param) {
        int photoCount = param.optInt("photoCount", 9);
        boolean showCamera = "1".equals(param.optString("showCamera", "0"));
        boolean previewEnabled = "1".equals(param.optString("previewEnabled", "1"));
        String[] items = new String[]{};
        JSONArray itemsJsonObject = param.optJSONArray("selectedPhotos");
        items = JsonUtil.parseJSONArray(itemsJsonObject, items);
        ArrayList<String> selectedPhotos = new ArrayList<>(Arrays.asList(items));
        ImageSelector.builder()
                .useCamera(showCamera) // 设置是否使用拍照
                .setSelected(selectedPhotos)
                .setSingle(false)  //设置是否单选
                .setMaxSelectCount(photoCount) // 图片的最大选择数量，小于等于0时，不限数量。
                .canPreview(previewEnabled) //是否可以预览图片，默认为true
                .start(this, ImageSelector.RESULT_CODE); // 打开相册
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        control.onResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        control.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        control.onPause();
    }

    @Override
    public void onDestroyView() {
        control.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onNbRight(View view, int which) {
        super.onNbRight(view, which);
        control.autoCallbackEvent.onClickNbRight(which);
    }

    @Override
    public void onNbLeft(View view) {
        super.onNbLeft(view);
        if (view.getTag() != null && "close".equals(view.getTag().toString())) {
            super.onNbBack();
        } else {
            control.autoCallbackEvent.onClickNbLeft();
        }
    }

    @Override
    public void onNbTitle(View view) {
        super.onNbTitle(view);
        control.autoCallbackEvent.onClickNbTitle(0);
    }

    @Override
    public void onNbBack() {
        if (control.autoCallbackEvent.isRegist(AutoCallbackDefined.OnClickNbBack)) {
            control.autoCallbackEvent.onClickNbBack();
        } else{
            control.loadLastPage(false);
        }
    }

    @Override
    public void onNbSearch(String keyWord) {
        super.onNbSearch(keyWord);
        keyWord = keyWord.replace("\\", "\\\\").replace("'", "\\'");
        Map<String, Object> object = new HashMap<>();
        object.put("keyword", keyWord);
        control.autoCallbackEvent.onSearch(object);
    }

}
