package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.config;


import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.*;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.*;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MapStructConfig {
    
    @Bean
    ITaxLiabilityMapper mapStructMapper() {return Mappers.getMapper(ITaxLiabilityMapper.class);}
    
    @Bean
    ITaxLiabilityRestMapper mapStructMapperRest() {return Mappers.getMapper(ITaxLiabilityRestMapper.class);}

    @Bean
    ITaxPayerTypeMapper mapStructTaxPayerTypeMapper() {return Mappers.getMapper(ITaxPayerTypeMapper.class);}

    @Bean
    ITaxPayerTypeRestMapper mapStructTaxPayerTypeMapperRest() {return Mappers.getMapper(ITaxPayerTypeRestMapper.class);}

    @Bean 
    IEnterpriseSearchMapper mapStructMapperEnterprise() {return Mappers.getMapper(IEnterpriseSearchMapper.class);}

    @Bean
    ICitiesbyDepartmentMapper mapStructMapperCitiesMapper(){return Mappers.getMapper( ICitiesbyDepartmentMapper.class);}
    @Bean
    ICitiesbyDepartmentRestMapper mapStructMapperCitiesRestMapper(){return Mappers.getMapper( ICitiesbyDepartmentRestMapper.class);}

    @Bean
    IDepartmentsMapper mapStructMapperDepartmetsMapper(){return Mappers.getMapper( IDepartmentsMapper.class);}
    
    @Bean
    IDepartmentRestMapper mapStructMapperDepartmetRestMappper(){return Mappers.getMapper( IDepartmentRestMapper.class); }

    @Bean
    IEnterpriseCreateMapper mapStructMapperEnterpriseCreate(){return Mappers.getMapper(IEnterpriseCreateMapper.class);}

    @Bean
    ILocationMapper mapStructMapperLocation(){return Mappers.getMapper(ILocationMapper.class);}

    @Bean
    IPersonTypeMapper mapStructMapperPersonType(){return Mappers.getMapper(IPersonTypeMapper.class);}

    @Bean
    IEnterpriseUpdateMapper mapStructMapperEnterpriseUpdate(){return Mappers.getMapper(IEnterpriseUpdateMapper.class);}

    @Bean
    IEnterpriseSearchRestMapper mapStructMapperEnterpriseSearchRest(){return Mappers.getMapper(IEnterpriseSearchRestMapper.class);}

}

