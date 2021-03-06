package artemshumidub.ru.news.ui.activity.newslist;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import artemshumidub.ru.news.R;
import artemshumidub.ru.news.data.entity.ShortNews;
import artemshumidub.ru.news.data.exception.NoInternetException;
import artemshumidub.ru.news.data.exception.ServerErrorException;
import artemshumidub.ru.news.data.remote.response.NewsListByCategoryResponse;
import artemshumidub.ru.news.data.repository.RemoteRepository;
import artemshumidub.ru.news.data.util.ConnectionUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewsListPresenter implements INewsListContract.IPresenter  {

    public static final int NEWS_PER_PAGE = 10;
    private static final String ALL_NEWS_GOT = "Все новости загружены";
    private INewsListContract.IView view;
    private RemoteRepository remoteRepository;
    private List<ShortNews> list;
    private boolean isLatsNewsGot = false;
    private Context appContext;
    ConnectionUtil connectionUtil;

    @Inject
    public NewsListPresenter(Context appContext, ConnectionUtil connectionUtil){
        this.appContext = appContext;
        this.connectionUtil = connectionUtil;
        list = new ArrayList<>();
    }

    @Override
    public void attachView(INewsListContract.IView view) {
        this.view = view;
    }

    @Override
    public void detachView() { this.view=null;   }

    @Override
    public void onStart() {    }

    @Override
    public void onStop() {   }

    @Override
    public void onResume() {   }

    @Override
    public void onPause() {   }

    @Override
    public void getFirstPageOfNewsList(long idCategory) {
        view.startProgress();
        if (!connectionUtil.checkInternetConnection()){
            view.showInternetError();
            return;
        }
        if (remoteRepository == null) {
            remoteRepository = new RemoteRepository(appContext);
        }
        Observable<NewsListByCategoryResponse> observable = remoteRepository.getNewsList(idCategory, 0);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NewsListByCategoryResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onNext(NewsListByCategoryResponse response) {
                        view.stopProgress();
                        isLatsNewsGot = response.getList().size() < NEWS_PER_PAGE;
                        view.setPage(0);
                        list.clear();
                        list.addAll(response.getList());
                        if (isLatsNewsGot && !list.isEmpty()) view.showMessage(ALL_NEWS_GOT);
                        if (response.getList().size()>=NEWS_PER_PAGE){
                            view.setPage(view.getPage()+1);
                        }
                        if (list.isEmpty()) view.showEmptyContentMessage();
                        else view.setNewsList(response.getList());
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.setPage(0);
                        if (e instanceof ServerErrorException) {
                            view.showServerError();
                        } else if (e instanceof NoInternetException) {
                            view.showInternetError();
                        } else {
                        view.showUnknownError();
                    }
                        view.setNewsListGetting(false);
                    }

                    @Override
                    public void onComplete() {
                        view.stopProgress();
                        view.setNewsListGetting(false);
                    }
                });
    }

    @Override
    public void getNextPageOfNewsList(long idCategory, int page) {
        if (isLatsNewsGot) return;
        view.showSmallProgressBar();
        if (!connectionUtil.checkInternetConnection()){
            view.showMessage(appContext.getResources().getString(R.string.internet_error));
            view.hideSmallProgressBar();
            return;
        }
        if (remoteRepository == null) {
            remoteRepository = new RemoteRepository(appContext);
        }
        Observable<NewsListByCategoryResponse> observable = remoteRepository.getNewsList(idCategory, page);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NewsListByCategoryResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {  }

                    @Override
                    public void onNext(NewsListByCategoryResponse response) {
                        if (response.getList().size() < NEWS_PER_PAGE){
                            isLatsNewsGot = true;
                            view.showMessage(ALL_NEWS_GOT);
                        }
                        if (page == 0) {
                            list.clear();
                        }
                        list.addAll(response.getList());
                        if (response.getList().size()>=NEWS_PER_PAGE){
                            view.setPage(view.getPage()+1);
                        }
                        if (list.isEmpty()) view.showEmptyContentMessage();
                        else view.setNextNewsList(list);
                        view.hideSmallProgressBar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.setPage(0);
                        if (e instanceof ServerErrorException) {
                            view.showMessage(appContext.getResources().getString(R.string.server_error));
                        } else if (e instanceof NoInternetException) {
                           view.showMessage(appContext.getResources().getString(R.string.internet_error));
                        } else {
                            view.showMessage(appContext.getResources().getString(R.string.unknown_error));
                    }
                        view.hideSmallProgressBar();
                        view.setNewsListGetting(false);
                    }

                    @Override
                    public void onComplete() {
                        view.hideSmallProgressBar();
                        view.setNewsListGetting(false);
                    }
                });
    }

    @Override
    public void goToNews(long idNews) {
        if (connectionUtil.checkInternetConnection()){
           view.goToNews(idNews);
        } else {
            view.showMessage(appContext.getResources().getString(R.string.internet_error));}
    }
}

