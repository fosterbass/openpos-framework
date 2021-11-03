package org.jumpmind.pos.persist;

public class Query<T> {

    private String name;
    private Class<? extends T> resultClass;
    private boolean useAnd = true;
    private int maxInParameters = 1000;

    public Query<T> result(Class<? extends T> resultClass) {
        this.resultClass = resultClass;
        return this;
    }

    public Class<? extends T> getResultClass() {
        return resultClass;
    }

    public void setResultClass(Class<? extends T> resultClass) {
        this.resultClass = resultClass;
    }
    
    
    public Query<T> named(String name) {
        this.name = name;
        return this;
    }

    public Query<T> useAnd(boolean useAnd) {
        this.useAnd = useAnd;
        return this;
    }
    
    public boolean isUseAnd() {
        return useAnd;
    }

    public String getName() {
        return name;
    }

    public void setMaxInParameters(int maxInParameters) {
        this.maxInParameters = maxInParameters;
    }

    public int getMaxInParameters() {
        return maxInParameters;
    }
}
