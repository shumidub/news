package artemshumidub.ru.sebbianews.ui.activity.news;

import javax.inject.Inject;

import artemshumidub.ru.sebbianews.data.exception.NoInternetException;
import artemshumidub.ru.sebbianews.data.exception.ServerErrorException;
import artemshumidub.ru.sebbianews.data.remote.response.NewsResponse;
import artemshumidub.ru.sebbianews.data.repository.RemoteRepository;
import artemshumidub.ru.sebbianews.data.util.ConnectionUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewsPresenter implements INewsContract.IPresenter {

    private INewsContract.IView view;
    private RemoteRepository remoteRepository;

    ConnectionUtil connectionUtil;

    @Inject
    public NewsPresenter(ConnectionUtil connectionUtil){
        this.connectionUtil = connectionUtil;
    }

    @Override
    public void attachView(INewsContract.IView view) {
        this.view = view;
    }

    @Override
    public void detachView() { this.view=null; }

    @Override
    public void onStart() {  }

    @Override
    public void onStop() {  }

    @Override
    public void onResume() {   }

    @Override
    public void onPause() {   }

    @Override
    public void getNews(long idNews) {
        view.startProgress();
        if (!connectionUtil.checkInternetConnection()) view.showInternetError();
        else {
            if (remoteRepository == null) { //todo dagger
                remoteRepository = new RemoteRepository((NewsActivity) view);
            }
            Observable<NewsResponse> observable = remoteRepository.getNews(idNews);
            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<NewsResponse>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(NewsResponse response) {
                            view.stopProgress();
                            if (response.getNews() != null
                                    && response.getNews().getTitle() != null
                                    && response.getNews().getFullDescription() != null){
                                view.setNews(response.getNews());
                            } else view.showEmptyContentMessage();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof ServerErrorException) {
                                view.showServerError();
                            } else if (e instanceof NoInternetException) {
                                view.showInternetError();
                            } else {
                                view.showUnknownError();
                            }
                        }

                        @Override
                        public void onComplete() {
                            view.stopProgress();
                        }
                    });
        }
    }
}
