package licola.demo.com.huabandemo.MyFollowing;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import licola.demo.com.huabandemo.API.OnBoardFragmentInteractionListener;
import licola.demo.com.huabandemo.API.OnRefreshFragmentInteractionListener;
import licola.demo.com.huabandemo.Adapter.RecyclerBoardAdapter;
import licola.demo.com.huabandemo.Base.BaseRecyclerHeadFragment;
import licola.demo.com.huabandemo.Bean.BoardPinsBean;
import licola.demo.com.huabandemo.HttpUtils.RetrofitService;
import licola.demo.com.huabandemo.Util.Logger;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by LiCola on  2016/04/04  21:39
 */
public class MyAttentionBoardFragment extends BaseRecyclerHeadFragment<RecyclerBoardAdapter, List<BoardPinsBean>> {
    private static final String TAG = "MyAttentionBoardFragment";

    private int mIndex = 1;//联网的起始页 默认1

    private OnBoardFragmentInteractionListener<BoardPinsBean> mListener;
    private OnRefreshFragmentInteractionListener mRefreshListener;

    @Override
    protected String getTAG() {
        return this.toString();
    }

    public static MyAttentionBoardFragment newInstance() {
        return new MyAttentionBoardFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Subscription getHttpFirst() {

        return RetrofitService.createAvatarService()
                .httpsMyFollowingBoardRx(mAuthorization,mIndex,mLimit)
                .map(new Func1<FollowingBoardListBean, List<BoardPinsBean>>() {
                    @Override
                    public List<BoardPinsBean> call(FollowingBoardListBean followingBoardListBean) {
                        return followingBoardListBean.getBoards();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(getFilterFunc1())
                .subscribe(new Subscriber<List<BoardPinsBean>>() {
                    @Override
                    public void onCompleted() {
                        Logger.d();
                        mRefreshListener.OnRefreshState(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                        checkException(e);
                        mRefreshListener.OnRefreshState(false);
                    }

                    @Override
                    public void onNext(List<BoardPinsBean> followingBoardItemBeen) {
                        Logger.d();
                        mAdapter.addList(followingBoardItemBeen);
                        mIndex++;
                    }
                });
    }

    @Override
    protected Subscription getHttpScroll() {
        return getHttpFirst();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mAdapter.setOnClickItemListener(new RecyclerBoardAdapter.onAdapterListener() {
            @Override
            public void onClickImage(BoardPinsBean bean, View view) {
                Logger.d();
                mListener.onClickBoardItemImage(bean, view);
            }

            @Override
            public void onClickTextInfo(BoardPinsBean bean, View view) {
                Logger.d();
                mListener.onClickBoardItemOperate(bean, view);
            }
        });
    }


    @Override
    protected View getHeadView() {
        return null;
    }

    @Override
    protected int getAdapterPosition() {
        return mAdapter.getAdapterPosition();
    }

    @Override
    protected RecyclerBoardAdapter setAdapter() {
        return new RecyclerBoardAdapter(mRecyclerView);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if ((context instanceof OnBoardFragmentInteractionListener)&&(context instanceof OnRefreshFragmentInteractionListener)) {
            mListener = (OnBoardFragmentInteractionListener<BoardPinsBean>) context;
            mRefreshListener= (OnRefreshFragmentInteractionListener) context;
        } else {
            throwRuntimeException(context);
        }

        if (context instanceof MyAttentionActivity){
            mAuthorization=((MyAttentionActivity) context).mAuthorization;
        }
    }

}