package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CollectionRun} and its DTO {@link CollectionRunDTO}.
 */
@Mapper(componentModel = "spring")
public interface CollectionRunMapper extends EntityMapper<CollectionRunDTO, CollectionRun> {
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userLogin")
    CollectionRunDTO toDto(CollectionRun s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
