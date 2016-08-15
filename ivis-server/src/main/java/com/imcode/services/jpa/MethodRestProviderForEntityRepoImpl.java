package com.imcode.services.jpa;

import com.imcode.entities.MethodRestProviderForEntity;
import com.imcode.repositories.MethodRestProviderForEntityRepository;
import com.imcode.services.AbstractNamedService;
import com.imcode.services.MethodRestProviderForEntityService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by ruslan on 01.08.16.
 */
@Service
public class MethodRestProviderForEntityRepoImpl extends AbstractNamedService<MethodRestProviderForEntity,
        Long, MethodRestProviderForEntityRepository> implements MethodRestProviderForEntityService{

    @Override
    public void deleteAll() {
        getRepo().deleteAll();
    }

    @Override
    public List<MethodRestProviderForEntity> findAllowedMethodsByClientId(String id) {
        return getRepo().findByClients_ClientId(id);
    }

    @Override
    public List<MethodRestProviderForEntity> findAllowedMethodsByUserId(Long id) {
        return getRepo().findByUsers_Id(id);
    }

    @Override
    public MethodRestProviderForEntity findAllowedMethod(
            String name,
            String entityName,
            String clientId,
            Long id
    ) {
        return getRepo().findByNameAndEntityRestProviderInformation_EntityClassAndClients_ClientIdAndUsers_Id(name, entityName, clientId, id);
    }
}
