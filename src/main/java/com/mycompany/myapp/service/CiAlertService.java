package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.CiAlert;
import com.mycompany.myapp.repository.CiAlertRepository;
import com.mycompany.myapp.service.dto.CiAlertDTO;
import com.mycompany.myapp.service.mapper.CiAlertMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CiAlertService {

    private static final Logger LOG = LoggerFactory.getLogger(CiAlertService.class);

    private final CiAlertRepository ciAlertRepository;

    private final CiAlertMapper ciAlertMapper;

    public CiAlertService(CiAlertRepository ciAlertRepository, CiAlertMapper ciAlertMapper) {
        this.ciAlertRepository = ciAlertRepository;
        this.ciAlertMapper = ciAlertMapper;
    }

    public CiAlertDTO save(CiAlertDTO ciAlertDTO) {
        LOG.debug("Request to save CiAlert : {}", ciAlertDTO);
        CiAlert ciAlert = ciAlertMapper.toEntity(ciAlertDTO);
        ciAlert = ciAlertRepository.save(ciAlert);
        return ciAlertMapper.toDto(ciAlert);
    }

    public CiAlertDTO update(CiAlertDTO ciAlertDTO) {
        LOG.debug("Request to update CiAlert : {}", ciAlertDTO);
        CiAlert ciAlert = ciAlertMapper.toEntity(ciAlertDTO);
        ciAlert = ciAlertRepository.save(ciAlert);
        return ciAlertMapper.toDto(ciAlert);
    }

    public Optional<CiAlertDTO> partialUpdate(CiAlertDTO ciAlertDTO) {
        LOG.debug("Request to partially update CiAlert : {}", ciAlertDTO);

        return ciAlertRepository
            .findById(ciAlertDTO.getId())
            .map(existingCiAlert -> {
                ciAlertMapper.partialUpdate(existingCiAlert, ciAlertDTO);

                return existingCiAlert;
            })
            .map(ciAlertRepository::save)
            .map(ciAlertMapper::toDto);
    }

    public Page<CiAlertDTO> findAllWithEagerRelationships(Pageable pageable) {
        return ciAlertRepository.findAllWithEagerRelationships(pageable).map(ciAlertMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<CiAlertDTO> findOne(Long id) {
        LOG.debug("Request to get CiAlert : {}", id);
        return ciAlertRepository.findOneWithEagerRelationships(id).map(ciAlertMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete CiAlert : {}", id);
        ciAlertRepository.deleteById(id);
    }
}
