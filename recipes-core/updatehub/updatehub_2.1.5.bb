SUMMARY = "A Firmware Over-The-Air agent for Embedded and Industrial Linux-based devices"
HOMEPAGE = "https://updatehub.io/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE-APACHE;md5=fa818a259cbed7ce8bc2a22d35a464fc"

DEPENDS = "libarchive openssl protobuf-native upx-native"

SRC_URI += " \
    git://github.com/UpdateHub/updatehub.git;protocol=https;nobranch=1\
    file://updatehub-local-update \
    file://updatehub-local-update-systemd.rules \
    file://updatehub-local-update-sysvinit.rules \
    file://updatehub-local-update.service \
    file://updatehub.initd \
    file://updatehub.service \
"

SRCREV = "9b40985b1622f3d21dd538d5c4829e1d95341e6e"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = "updatehub"

inherit systemd update-rc.d pkgconfig cargo cargo-update-recipe-crates

require updatehub-crates.inc

PACKAGECONFIG ?= "backward-compatibility"
PACKAGECONFIG[backward-compatibility] = "v1-parsing"

CARGO_FEATURES = "${PACKAGECONFIG_CONFARGS}"
EXTRA_CARGO_FLAGS = "--bin ${BPN}"

SYSTEMD_PACKAGE = "${BPN}"
SYSTEMD_SERVICE:${BPN} = "${BPN}.service"

INITSCRIPT_NAME = "${BPN}"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 ."

SYSTEMD_PACKAGE:updatehub-local-update = "updatehub-local-update"
SYSTEMD_SERVICE:updatehub-local-update = "updatehub-local-update@.service"
SYSTEMD_AUTO_ENABLE:updatehub-local-update = "disable"

UPX ?= "${STAGING_BINDIR_NATIVE}/upx"
UPX_ARGS ?= "--best -q"

UPDATEHUB_LOCAL_UPDATE_DIR ??= "/mnt/updatehub"

do_install:append() {
    install -Dm 0755 ${UNPACKDIR}/updatehub-local-update ${D}${bindir}/updatehub-local-update
    sed -i -e 's,@LOCAL_UPDATE_DIR@,${UPDATEHUB_LOCAL_UPDATE_DIR},g' ${D}${bindir}/updatehub-local-update

    # Handle init system integration and updatehub local update udev rule for USB mounting
    if ${@bb.utils.contains('DISTRO_FEATURES','sysvinit','true','false',d)}; then
        install -Dm 0755 ${UNPACKDIR}/updatehub.initd ${D}${sysconfdir}/init.d/updatehub
        install -Dm 0644 ${UNPACKDIR}/updatehub-local-update-sysvinit.rules ${D}${nonarch_base_libdir}/udev/rules.d/99-updatehub.rules
        sed -i -e 's,@BINDIR@,${bindir},g' \
            -e 's,@LIBDIR@,${libdir},g' \
            -e 's,@LOCALSTATEDIR@,${localstatedir},g' \
            -e 's,@SYSCONFDIR@,${sysconfdir},g' \
            ${D}/${sysconfdir}/init.d/updatehub
    fi
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -Dm 0644 ${UNPACKDIR}/updatehub.service ${D}${systemd_system_unitdir}/updatehub.service
        install -Dm 0644 ${UNPACKDIR}/updatehub-local-update.service ${D}${systemd_system_unitdir}/updatehub-local-update@.service
        install -Dm 0644 ${UNPACKDIR}/updatehub-local-update-systemd.rules ${D}${nonarch_base_libdir}/udev/rules.d/99-updatehub.rules
        sed -i -e 's,@BINDIR@,${bindir},g' \
            ${D}${systemd_system_unitdir}/updatehub.service \
            ${D}${systemd_system_unitdir}/updatehub-local-update@.service
    fi
    sed -i -e 's,@LOCAL_UPDATE_DIR@,${UPDATEHUB_LOCAL_UPDATE_DIR},g' \
        ${D}${nonarch_base_libdir}/udev/rules.d/99-updatehub.rules
}

apply_upx[vardeps] += "UPX UPX_ARGS"
apply_upx() {
   ${UPX} ${UPX_ARGS} ${PKGDEST}/${BPN}/${bindir}/updatehub
}

PACKAGEFUNCS += "apply_upx"

PACKAGES =+ "${BPN}-local-update"

# Now, the same updatehub binary works as server and client tool, so replacing
# the old updatehub-ctl.
RREPLACES:${BPN} += "${BPN}-ctl"
RPROVIDES:${BPN} += "${BPN}-ctl"
RCONFLICTS:${BPN} += "${BPN}-ctl"

FILES:${BPN}-local-update += " \
    ${nonarch_base_libdir}/udev/rules.d/99-updatehub.rules \
    ${systemd_system_unitdir}/updatehub-local-update@.service \
"

BBCLASSEXTEND = "native nativesdk"
