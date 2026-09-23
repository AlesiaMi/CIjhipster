package com.mycompany.myapp.service.importer.parser;

import com.mycompany.myapp.service.importer.ImportParseException;
import com.mycompany.myapp.service.importer.model.ImportFormat;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@Component
public class XmlImportParser implements ImportParser {

    @Override
    public ImportFormat format() {
        return ImportFormat.XML;
    }

    @Override
    public List<ImportRecord> parse(MultipartFile file, XmlImportDefinition xmlDefinition) {
        if (xmlDefinition == null) {
            throw new ImportParseException("XML import is not configured for this entity");
        }

        try {
            DocumentBuilderFactory factory = createSecureValidatingFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();

            builder.setEntityResolver(localDtdResolver(xmlDefinition));
            builder.setErrorHandler(strictErrorHandler());
            Document document = builder.parse(file.getInputStream());
            Element root = document.getDocumentElement();
            if (!xmlDefinition.rootElement().equals(root.getTagName())) {
                throw new ImportParseException("XML root element must be <" + xmlDefinition.rootElement() + ">");
            }
            List<ImportRecord> records = new ArrayList<>();
            NodeList children = root.getChildNodes();
            int rowNumber = 1;
            for (int index = 0; index < children.getLength(); index++) {
                Node node = children.item(index);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element recordElement = (Element) node;
                if (!xmlDefinition.recordElement().equals(recordElement.getTagName())) {
                    throw new ImportParseException(rowNumber, "Unexpected XML element: " + recordElement.getTagName());
                }
                Map<String, String> values = new LinkedHashMap<>();
                NodeList fields = recordElement.getChildNodes();
                for (int fieldIndex = 0; fieldIndex < fields.getLength(); fieldIndex++) {
                    Node fieldNode = fields.item(fieldIndex);
                    if (fieldNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    Element fieldElement = (Element) fieldNode;
                    if (values.containsKey(fieldElement.getTagName())) {
                        throw new ImportParseException(rowNumber, "Duplicate XML field: " + fieldElement.getTagName());
                    }
                    values.put(fieldElement.getTagName(), fieldElement.getTextContent());
                }
                records.add(new ImportRecord(rowNumber, values));
                rowNumber++;
            }
            return records;
        } catch (ImportParseException exception) {
            throw exception;
        } catch (SAXParseException exception) {
            throw new ImportParseException(exception.getLineNumber(), "XML/DTD validation error: " + exception.getMessage(), exception);
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new ImportParseException("Unable to parse XML: " + exception.getMessage(), exception);
        }
    }

    private DocumentBuilderFactory createSecureValidatingFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private EntityResolver localDtdResolver(XmlImportDefinition definition) {
        return (publicId, systemId) -> {
            if (systemId == null || (!systemId.endsWith("/" + definition.dtdSystemId()) && !systemId.equals(definition.dtdSystemId()))) {
                throw new SAXException("External DTD is not allowed: " + systemId);
            }
            ClassPathResource resource = new ClassPathResource(definition.dtdClasspathResource());
            InputStream inputStream = resource.getInputStream();
            InputSource source = new InputSource(inputStream);
            source.setPublicId(publicId);
            source.setSystemId(definition.dtdSystemId());
            return source;
        };
    }

    private ErrorHandler strictErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }
        };
    }
}
