///////////////////////////////////////////////////////////////////////////////////////////////////////
package com.persistance.Template;
//  FILE : TemplateDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalCS>
//  DESCRIPTION: Is a template DAO.java INTERFACE file, used to DECLARE functions
//               USE the functins declared here in the CONTROLLER file, 
//               DEFINE these functions in the FileDAO.java file with @override
//
///////////////////////////////////////////////////////////////////////////////////////////////////////
import java.io.IOException;

import org.springframework.stereotype.Repository;
import com.model.Template;


@Repository
public interface TemplateDAO{
    Template[] getTemplates(int temid) throws IOException;
    Boolean createTemplate(String tname, String tmess, Boolean tbool)throws IOException;
    Boolean updateTemplate(int temid, String tname, String tmess, Boolean tbool)throws IOException;
    Boolean deleteTemplate(int temid) throws IOException;
}