package artemshumidub.ru.sebbianews.ui.activity.category;

import artemshumidub.ru.sebbianews.SebbiaNewsApp;
import artemshumidub.ru.sebbianews.data.entity.Category;
import artemshumidub.ru.sebbianews.data.exception.NoInternetException;
import artemshumidub.ru.sebbianews.data.exception.ServerErrorException;
import artemshumidub.ru.sebbianews.data.remote.response.CategoryResponse;
import artemshumidub.ru.sebbianews.data.repository.RemoteRepository;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CategoriesPresenter implements ICategoriesContract.IPresenter {

    private ICategoriesContract.IView view;
    private RemoteRepository remoteRepository;

    CategoriesPresenter(ICategoriesContract.IView view){
        attachView(view);
    }

    @Override
    public void attachView(ICategoriesContract.IView view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    @SuppressWarnings("unused")
    public void onStart() {

    }

    @Override
    public void onStop() {
        detachView();
    }

    @Override
    @SuppressWarnings("unused")
    public void onResume() {

    }

    @Override
    @SuppressWarnings("unused")
    public void onPause() {

    }

    @Override
    public void getCategories() {
        view.startProgress();
        if(!SebbiaNewsApp.getConnectionUtil().checkInternetConnection()) view.showInternetError();
        else {

            if (remoteRepository == null){
                remoteRepository = new RemoteRepository((CategoriesActivity) view);
            }

            Observable<CategoryResponse> observable = remoteRepository.getCategory();
            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<CategoryResponse>() {
                        @Override
                        public void onSubscribe(Disposable d) {  }

                        @Override
                        public void onNext(CategoryResponse response) {
                            view.stopProgress();
                            if (response.getList().size() == 0) view.showEmptyContentMessage();
                            else view.setCategories(response.getList());
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof ServerErrorException){view.showServerError();}
                            else if (e instanceof NoInternetException){view.showInternetError();}
                        }

                        @Override
                        public void onComplete() {
                            view.stopProgress();
                        }
                    });
        }
    }
}
