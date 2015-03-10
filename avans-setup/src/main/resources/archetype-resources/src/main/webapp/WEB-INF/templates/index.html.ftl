<#-- @ftlvariable name="jdbcMajorVersion" type="java.lang.String" -->
<#-- @ftlvariable name="db" type="java.lang.String" -->
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#include "include/header.html.ftl">

<div class="row">
    Hello, world!
    <p>${name}</p>
    <p>${db}</p>

    <p>${jdbcMajorVersion}</p>
</div>

<#include "include/footer.html.ftl">
