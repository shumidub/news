package artemshumidub.ru.sebbianews.data.repository;

import android.content.Context;

import artemshumidub.ru.sebbianews.data.entity.ShortNews;
import artemshumidub.ru.sebbianews.data.remote.RetrofitHelper;
import artemshumidub.ru.sebbianews.data.remote.api.NewsApi;
import artemshumidub.ru.sebbianews.data.remote.response.CategoryResponse;
import artemshumidub.ru.sebbianews.data.remote.response.NewsListByCategoryResponse;
import artemshumidub.ru.sebbianews.data.remote.response.NewsResponse;
import io.reactivex.Observable;

public class RemoteRepository implements IRemoteRepositoryContract {

    private static NewsApi newsApiClient;

    public RemoteRepository(Context context) {
        if (newsApiClient == null) newsApiClient = new RetrofitHelper().getRestService(context);
    }

    @Override
    public Observable<CategoryResponse> getCategory() {
        return newsApiClient.getListOfCategories();
    }

    @Override
    public Observable<NewsListByCategoryResponse> getNewsList(long categoryId, int page) {
        return newsApiClient.getNewsList(categoryId, page);
    }

    @Override
    public Observable<NewsResponse> getNews(long idNews) {
        return newsApiClient.getNews(idNews);
    }

}
