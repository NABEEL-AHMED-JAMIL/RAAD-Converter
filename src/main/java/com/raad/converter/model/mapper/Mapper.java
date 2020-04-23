package com.raad.converter.model.mapper;

public abstract class Mapper<V, E> {

    public abstract V mapToVo(E e);

    public abstract E mapToEntity(V v);

}