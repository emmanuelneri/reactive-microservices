package br.com.emmanuelneri.reactivemicroservices.mapper;

import org.modelmapper.ModelMapper;

public interface MapperBuilder {

    ModelMapper INSTANCE = new ModelMapper();

}
