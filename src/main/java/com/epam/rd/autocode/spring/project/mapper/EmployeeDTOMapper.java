package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmployeeDTOMapper {
    private ModelMapper mapper;

    public EmployeeDTOMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
        log.debug("Mapping Employee to EmployeeDTO for email: {}", employee.getEmail());
        return mapper.map(employee, EmployeeDTO.class);
    }

    public Employee convertEmployeeDTOToEmployee(EmployeeDTO employeeDTO) {
        log.debug("Mapping EmployeeDTO to Employee for email: {}", employeeDTO.getEmail());
        return mapper.map(employeeDTO, Employee.class);
    }
}
