package org.entando.kubernetes.model.bundle.descriptor;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentTypeAttribute {

    private String code;
    private String type;
    private String name;
    private List<Role> roles;
    private List<String> disablingCodes;
    private Boolean mandatory;
    private Boolean listFilter;
    private Boolean indexable;

    private String enumeratorExtractorBean;
    private String enumeratorStaticItems;
    private String enumeratorStaticItemsSeparator;

    private ContentTypeValidationRule validationRules;

    private ContentTypeAttribute nestedAttribute;
    private List<ContentTypeAttribute> compositeAttributes;

}
