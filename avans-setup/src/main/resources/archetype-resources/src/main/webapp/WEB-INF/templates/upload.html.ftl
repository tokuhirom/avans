<#-- @ftlvariable name="name" type="java.lang.String" -->
<#include "include/header.html.ftl">

<div class="row">
    <form method="post" enctype="multipart/form-data">
        <input type="file" name="target" required>
        <input type="submit" value="Upload" class="btn btn-primary">
    </form>
</div>

<#include "include/footer.html.ftl">
