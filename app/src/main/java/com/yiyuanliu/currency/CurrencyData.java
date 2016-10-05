package com.yiyuanliu.currency;


import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.Callback;

/**
 * Created by yiyuan on 2016/10/3.
 */

public class CurrencyData {
    public static void load(Callback<CurrencyData> callback) {
        Retrofit.Builder builder = new Retrofit.Builder();
        Api api = builder.baseUrl("http://finance.yahoo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(Api.class);
        api.loadLatest().enqueue(callback);
    }

    interface Api {
        @GET("webservice/v1/symbols/allcurrencies/quote?format=json")
        Call<CurrencyData> loadLatest();
    }

    private ListBean list;

    public ListBean getList() {
        return list;
    }

    public void setList(ListBean list) {
        this.list = list;
    }

    public static class ListBean {

        private MetaBean meta;

        private List<ResourcesBean> resources;

        public MetaBean getMeta() {
            return meta;
        }

        public void setMeta(MetaBean meta) {
            this.meta = meta;
        }

        public List<ResourcesBean> getResources() {
            return resources;
        }

        public void setResources(List<ResourcesBean> resources) {
            this.resources = resources;
        }

        public static class MetaBean {
            private String type;
            private int start;
            private int count;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public int getStart() {
                return start;
            }

            public void setStart(int start) {
                this.start = start;
            }

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }
        }

        public static class ResourcesBean {

            private ResourceBean resource;

            public ResourceBean getResource() {
                return resource;
            }

            public void setResource(ResourceBean resource) {
                this.resource = resource;
            }

            public static class ResourceBean {
                private String classname;

                private FieldsBean fields;

                public String getClassname() {
                    return classname;
                }

                public void setClassname(String classname) {
                    this.classname = classname;
                }

                public FieldsBean getFields() {
                    return fields;
                }

                public void setFields(FieldsBean fields) {
                    this.fields = fields;
                }

                public static class FieldsBean {
                    private String name;
                    private String price;
                    private String symbol;
                    private String ts;
                    private String type;
                    private String utctime;
                    private String volume;

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getPrice() {
                        return price;
                    }

                    public void setPrice(String price) {
                        this.price = price;
                    }

                    public String getSymbol() {
                        return symbol;
                    }

                    public void setSymbol(String symbol) {
                        this.symbol = symbol;
                    }

                    public String getTs() {
                        return ts;
                    }

                    public void setTs(String ts) {
                        this.ts = ts;
                    }

                    public String getType() {
                        return type;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    public String getUtctime() {
                        return utctime;
                    }

                    public void setUtctime(String utctime) {
                        this.utctime = utctime;
                    }

                    public String getVolume() {
                        return volume;
                    }

                    public void setVolume(String volume) {
                        this.volume = volume;
                    }
                }
            }
        }
    }
}
