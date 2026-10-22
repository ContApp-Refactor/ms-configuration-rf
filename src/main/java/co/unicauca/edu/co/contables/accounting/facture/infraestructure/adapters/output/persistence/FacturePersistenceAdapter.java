package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureCreatedOutputPort;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureGetOutputPort;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Product;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.FactureEntity;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.ProductEntity;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.mapper.FacturePersistenceMapper;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.repository.FactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FacturePersistenceAdapter implements FactureCreatedOutputPort, FactureGetOutputPort {

    @PersistenceContext
    private EntityManager entityManager;

    private final FactureRepository factureRepository;
    private final ProductRepository productRepository;

    private final FacturePersistenceMapper facturePersistenceMapper;

    /**
     * Recupera el código de factura máximo de la base de datos. Este método se
     * utiliza para
     * generar el siguiente código de factura al crear una nueva factura.
     *
     * @return el código de factura máximo o null si no existen facturas
     */
    @Override
    public Long findMaxFactCode() {
        return factureRepository.findMaxFactCode();
    }

    @Override
    @Transactional
    public Facture saveFacture(Facture facture) {

        if (facture.getFactProducts().isEmpty()) {
            return null;
        }

        Set<Product> productsAux = facture.getFactProducts();

        FactureEntity factureEntity = this.facturePersistenceMapper.toFactureEntity(facture);

        Set<Long> productIds = facture.getFactProducts().stream()
                .map(Product::getProductId)
                .collect(Collectors.toSet());
        Set<ProductEntity> products = productRepository.findAllById(productIds).stream().collect(Collectors.toSet());

        // listar los id de los producros que ya estan en la base de datos
        Set<Long> productIdsExist = products.stream()
                .map(ProductEntity::getProductId)
                .collect(Collectors.toSet());

        factureEntity.getFactProducts().clear();

        for (Product product : productsAux) {
            if (!productIdsExist.contains(product.getProductId())) {
                products.add(this.facturePersistenceMapper.toProductEntity(product));
            }
        }

        factureEntity.getFactProducts().addAll(products);

        factureRepository.save(factureEntity);

        return this.convertToFacture(factureEntity);
    }

    /**
     * Recupera una Factura por su ID desde la base de datos.
     *
     * @param factId el ID de la Factura a recuperar.
     * @return un Optional que contiene la Factura si se encuentra, o un Optional
     *         vacío
     *         si no se encuentra ninguna Factura con el ID dado.
     */
    @Override
    public Optional<Facture> getFactureById(Long factId) {
        Optional<FactureEntity> factureOptional = this.factureRepository.findById(factId);
        if (factureOptional.isEmpty()) {
            return Optional.empty();
        }

        FactureEntity factureEntity = factureOptional.get();
        factureEntity.setFactProducts(this.productRepository.getProductsByFactureId(factId));

        Facture facture = this.facturePersistenceMapper.toFacture(factureEntity);

        return Optional.of(facture);
    }

    /**
     * Recupera una lista paginada de todas las facturas asociadas a una empresa
     * específica.
     *
     * @param entId    el ID de la empresa cuyas facturas se van a recuperar
     * @param pageable la configuración de paginación
     * @return una lista paginada de facturas asociadas al ID de empresa dado
     */
    @Override
    public Page<Facture> getAllFacturesBy(String entId, Pageable pageable) {
        Page<FactureEntity> pageFactureEntities = this.factureRepository.findAllByEnterpriseId(entId, pageable);
        return pageFactureEntities.map(this::convertToFacture);
    }

    /**
     * Recupera una lista paginada de todas las facturas de ventas asociadas a una
     * empresa específica.
     *
     * @param entId    el ID de la empresa cuyas facturas de ventas se van a
     *                 recuperar
     * @param pageable la configuración de paginación
     * @return una lista paginada de facturas de ventas asociadas al ID de empresa
     *         dado
     */
    @Override
    public Page<Facture> getAllSalesFacturesBy(String entId, Pageable pageable) {
        Page<FactureEntity> pageFactureEntities = this.factureRepository.findAllSalesFacturesByEnterpriseId(entId,
                pageable);
        return pageFactureEntities.map(this::convertToFacture);
    }

    /**
     * Recupera una lista paginada de todas las facturas de compras asociadas a una
     * empresa específica.
     *
     * @param entId    el ID de la empresa cuyas facturas de compras se van a
     *                 recuperar
     * @param pageable la configuración de paginación
     * @return una lista paginada de facturas de compras asociadas al ID de empresa
     *         dado
     */
    @Override
    public Page<Facture> getAllShoppingFacturesBy(String entId, Pageable pageable) {
        Page<FactureEntity> pageFactureEntities = this.factureRepository.findAllShoppingFacturesByEnterpriseId(entId,
                pageable);
        return pageFactureEntities.map(this::convertToFacture);
    }

    /**
     * Convierte una entidad de factura en un objeto de negocio Facture.
     *
     * @param factureEntity la entidad de factura a convertir
     * @return el objeto de negocio Facture correspondiente
     */
    private Facture convertToFacture(FactureEntity factureEntity) {
        factureEntity.setFactProducts(this.productRepository.getProductsByFactureId(factureEntity.getFactId()));
        return this.facturePersistenceMapper.toFacture(factureEntity);
    }
}
